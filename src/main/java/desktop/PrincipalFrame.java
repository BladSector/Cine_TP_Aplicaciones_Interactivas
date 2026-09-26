package desktop;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PrincipalFrame extends JFrame {
    private final ApiClient apiClient;
    private final String rol;
    private final JLabel estado = new JLabel("Listo");
    private final JTabbedPane pestanias = new JTabbedPane();
    private AdministracionPanel administracionPanel;

    private final DefaultTableModel salasModel = modelo("ID", "Nombre", "Capacidad", "Estado");
    private final DefaultTableModel funcionesModel = modelo(
            "ID", "Fecha", "Horario", "Película", "Sala", "Formato", "Idioma", "Precio"
    );
    private final DefaultTableModel butacasModel = modelo("ID", "Butaca", "Estado", "Sala");
    private final DefaultTableModel consumosModel = modelo(
            "ID", "Producto", "Cantidad", "Ticket", "Estado", "Subtotal"
    );
    private final DefaultTableModel empleadosModel = modelo("ID", "Nombre", "Apellido", "Usuario", "Activo");

    private final JTable salasTabla = tabla(salasModel);
    private final JTable funcionesTabla = tabla(funcionesModel);
    private final JTable butacasTabla = tabla(butacasModel);
    private final JTable consumosTabla = tabla(consumosModel);
    private final JTable empleadosTabla = tabla(empleadosModel);
    private final JEditorPane detalleSala = crearVisorDetalle("Seleccioná una sala para ver su información.");
    private final JEditorPane detalleConsumo = crearVisorDetalle("Seleccioná un consumo para ver su información.");

    private final JComboBox<SalaOpcion> salaButacas = new JComboBox<>();
    private final JPanel mapaButacas = new JPanel(new BorderLayout());
    private final JLabel resumenButacas = new JLabel("Seleccioná una sala.");
    private List<JsonNode> butacasActuales = new ArrayList<>();
    private Integer butacaSeleccionadaId;

    private final JTextField codigoQr = new JTextField(32);
    private final JTextArea detalleTicket = new JTextArea();
    private Integer ticketActualId;

    public PrincipalFrame(ApiClient apiClient, String rol, String usuario) {
        super("Cine API - " + nombreRol(rol));
        this.apiClient = apiClient;
        this.rol = rol;
        construirVista(usuario);
        cargarTodo();
    }

    private void construirVista(String usuario) {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1220, 790);
        setLocationRelativeTo(null);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel titulo = new JLabel("Administración del cine");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JToolBar herramientas = new JToolBar();
        herramientas.setFloatable(false);
        herramientas.setBorderPainted(false);
        JLabel sesion = new JLabel(usuario + " | " + nombreRol(rol));
        JButton recargar = new JButton("Recargar");
        JButton cerrarSesion = new JButton("Cerrar sesión");
        recargar.addActionListener(event -> cargarTodo());
        cerrarSesion.addActionListener(event -> cerrarSesion());
        herramientas.add(sesion);
        herramientas.addSeparator();
        herramientas.add(recargar);
        herramientas.add(cerrarSesion);
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(herramientas, BorderLayout.EAST);

        if (esDuenio()) {
            administracionPanel = new AdministracionPanel(apiClient, pestanias, estado::setText);
            int indiceSalas = indicePestania("Salas");
            pestanias.insertTab("Butacas", null, crearPanelButacas(), null, indiceSalas + 1);
        } else {
            pestanias.addTab("Salas", crearPanelSalas());
            pestanias.addTab("Butacas", crearPanelButacas());
            pestanias.addTab("Funciones", crearPanelFunciones());
        }
        if (esDuenio() || esEmpleado()) {
            pestanias.addTab("Tickets y QR", crearPanelTickets());
            pestanias.addTab("Consumos", crearPanelConsumos());
        }
        if (esDuenio()) {
            pestanias.addTab("Empleados", crearPanelEmpleados());
        }
        instalarDetallesDobleClick();
        setJMenuBar(crearMenu());

        estado.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        estado.setOpaque(true);
        estado.setBackground(new Color(238, 238, 238));
        add(cabecera, BorderLayout.NORTH);
        add(pestanias, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salirAplicacion();
            }
        });
    }

    private JPanel crearPanelSalas() {
        JPanel panel = panelConTablaYDetalle("Salas del cine", salasTabla, detalleSala);
        salasTabla.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                detalleSala.setText(descripcionSalaSeleccionada());
                detalleSala.setCaretPosition(0);
            }
        });
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> estadoSala = new JComboBox<>(new String[]{
                "DISPONIBLE", "LIMPIEZA", "CERRADA", "FUERA_DE_SERVICIO"
        });
        JButton cambiar = new JButton("Cambiar estado");
        JButton actualizar = new JButton("Recargar");
        cambiar.addActionListener(event -> cambiarEstadoSala(String.valueOf(estadoSala.getSelectedItem())));
        actualizar.addActionListener(event -> cargarSalas());
        acciones.add(new JLabel("Nuevo estado:"));
        acciones.add(estadoSala);
        acciones.add(cambiar);
        acciones.add(actualizar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFunciones() {
        JPanel panel = panelConTabla("Funciones programadas", funcionesTabla);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (esEmpleado()) {
            JButton modificarHorario = new JButton("Modificar horario");
            modificarHorario.addActionListener(event -> mostrarFormularioHorario());
            acciones.add(modificarHorario);
        }
        JButton actualizar = new JButton("Recargar funciones");
        actualizar.addActionListener(event -> cargarFunciones());
        acciones.add(actualizar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelButacas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel superior = new JPanel(new BorderLayout(12, 0));
        JLabel titulo = new JLabel("Plano de butacas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));
        JPanel selector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton actualizar = new JButton("Recargar");
        actualizar.addActionListener(event -> {
            cargarSalas();
            cargarButacas();
        });
        salaButacas.addActionListener(event -> actualizarMapaButacas());
        selector.add(new JLabel("Sala:"));
        selector.add(salaButacas);
        selector.add(actualizar);
        superior.add(titulo, BorderLayout.WEST);
        superior.add(selector, BorderLayout.EAST);

        JScrollPane scrollMapa = new JScrollPane(mapaButacas);
        scrollMapa.getVerticalScrollBar().setUnitIncrement(18);
        scrollMapa.getHorizontalScrollBar().setUnitIncrement(18);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton ocupar = new JButton("Ocupar");
        JButton liberar = new JButton("Liberar");
        JButton fueraServicio = new JButton("Fuera de servicio");
        ocupar.addActionListener(event -> cambiarEstadoButaca("ocupar"));
        liberar.addActionListener(event -> cambiarEstadoButaca("liberar"));
        fueraServicio.addActionListener(event -> cambiarEstadoButaca("fuera-de-servicio"));
        acciones.add(resumenButacas);
        acciones.add(Box.createHorizontalStrut(18));
        acciones.add(ocupar);
        acciones.add(liberar);
        acciones.add(fueraServicio);

        panel.add(superior, BorderLayout.NORTH);
        panel.add(scrollMapa, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTickets() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton buscar = new JButton("Buscar QR");
        buscar.addActionListener(event -> buscarTicket());
        codigoQr.addActionListener(event -> buscarTicket());
        busqueda.add(new JLabel("Código QR:"));
        busqueda.add(codigoQr);
        busqueda.add(buscar);

        detalleTicket.setEditable(false);
        detalleTicket.setLineWrap(true);
        detalleTicket.setWrapStyleWord(true);
        detalleTicket.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton validar = new JButton("Validar entradas");
        JButton entregar = new JButton("Entregar consumos");
        validar.addActionListener(event -> operarTicket("validar-entradas"));
        entregar.addActionListener(event -> operarTicket("entregar-consumos"));
        acciones.add(validar);
        acciones.add(entregar);

        panel.add(busqueda, BorderLayout.NORTH);
        panel.add(new JScrollPane(detalleTicket), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelConsumos() {
        JPanel panel = panelConTablaYDetalle("Consumos pendientes y entregados", consumosTabla, detalleConsumo);
        consumosTabla.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                detalleConsumo.setText(descripcionFila(consumosTabla));
                detalleConsumo.setCaretPosition(0);
            }
        });
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton entregar = new JButton("Marcar entregado");
        JButton actualizar = new JButton("Recargar");
        entregar.addActionListener(event -> entregarConsumoSeleccionado());
        actualizar.addActionListener(event -> cargarConsumos());
        acciones.add(entregar);
        acciones.add(actualizar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelEmpleados() {
        JPanel panel = panelConTabla("Personal del cine", empleadosTabla);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton crear = new JButton("Nuevo empleado");
        JButton activar = new JButton("Activar");
        JButton desactivar = new JButton("Desactivar");
        JButton actualizar = new JButton("Recargar");
        crear.addActionListener(event -> mostrarFormularioEmpleado());
        activar.addActionListener(event -> cambiarEstadoEmpleado("activar"));
        desactivar.addActionListener(event -> cambiarEstadoEmpleado("desactivar"));
        actualizar.addActionListener(event -> cargarEmpleados());
        acciones.add(crear);
        acciones.add(activar);
        acciones.add(desactivar);
        acciones.add(actualizar);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel panelConTabla(String titulo, JTable tabla) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 15f));
        JTextField filtro = new JTextField(24);
        filtro.putClientProperty("JTextField.placeholderText", "Buscar...");
        cabecera.add(etiqueta, BorderLayout.WEST);
        cabecera.add(filtro, BorderLayout.EAST);
        conectarFiltro(tabla, filtro);

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelConTablaYDetalle(String titulo, JTable tabla, JEditorPane detalle) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel tablaPanel = panelConTabla(titulo, tabla);
        tablaPanel.setBorder(BorderFactory.createEmptyBorder());

        JPanel detallePanel = new JPanel(new BorderLayout(0, 8));
        detallePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        detallePanel.setPreferredSize(new Dimension(260, 0));
        JLabel tituloDetalle = new JLabel("Información");
        tituloDetalle.setFont(tituloDetalle.getFont().deriveFont(Font.BOLD));
        detallePanel.add(tituloDetalle, BorderLayout.NORTH);
        detallePanel.add(new JScrollPane(detalle), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablaPanel, detallePanel);
        division.setResizeWeight(0.78);
        division.setDividerLocation(0.78);
        division.setBorder(BorderFactory.createEmptyBorder());
        panel.add(division, BorderLayout.CENTER);
        return panel;
    }

    private void instalarDetallesDobleClick() {
        instalarDetalleDobleClick(salasTabla, "Información de la sala", this::descripcionSalaSeleccionada,
                "Cambiar estado", this::mostrarSelectorEstadoSala);
        instalarDetalleDobleClick(funcionesTabla, "Información de la función",
                () -> descripcionFila(funcionesTabla),
                "Modificar horario", this::mostrarFormularioHorario);
        instalarDetalleDobleClick(consumosTabla, "Información del consumo",
                () -> descripcionFila(consumosTabla), "Marcar entregado", this::entregarConsumoSeleccionado);
        instalarDetalleDobleClick(empleadosTabla, "Información del empleado",
                () -> descripcionFila(empleadosTabla));
    }

    private void instalarDetalleDobleClick(JTable tabla, String titulo, Supplier<String> descripcion) {
        instalarDetalleDobleClick(tabla, titulo, descripcion, null, null);
    }

    private void instalarDetalleDobleClick(JTable tabla, String titulo, Supplier<String> descripcion,
                                            String accionTexto, Runnable accion) {
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() != 2 || event.getButton() != java.awt.event.MouseEvent.BUTTON1) {
                    return;
                }
                int fila = tabla.rowAtPoint(event.getPoint());
                if (fila >= 0) {
                    tabla.setRowSelectionInterval(fila, fila);
                    if (accion == null) {
                        mostrarDetalle(titulo, descripcion.get());
                    } else {
                        mostrarDetalleConAccion(titulo, descripcion.get(), accionTexto, accion);
                    }
                }
            }
        });
    }

    private String descripcionSalaSeleccionada() {
        int filaVista = salasTabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná una sala para ver su información.");
        }
        int fila = salasTabla.convertRowIndexToModel(filaVista);
        String nombreSala = String.valueOf(salasModel.getValueAt(fila, 1));

        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("SALA"))
                .append(campoHtml("Nombre", nombreSala))
                .append(campoHtml("Capacidad", salasModel.getValueAt(fila, 2)))
                .append(campoHtml("Estado", salasModel.getValueAt(fila, 3)))
                .append(seccionHtml("FUNCIONES PROGRAMADAS"));

        int cantidadFunciones = 0;
        for (int i = 0; i < funcionesModel.getRowCount(); i++) {
            if (!nombreSala.equals(String.valueOf(funcionesModel.getValueAt(i, 4)))) {
                continue;
            }
            cantidadFunciones++;
            descripcion.append(itemHtml(funcionesModel.getValueAt(i, 3)))
                    .append(campoHtml("Fecha", funcionesModel.getValueAt(i, 1)))
                    .append(campoHtml("Horario", funcionesModel.getValueAt(i, 2)))
                    .append(campoHtml("Formato", funcionesModel.getValueAt(i, 5)))
                    .append(campoHtml("Idioma", funcionesModel.getValueAt(i, 6)))
                    .append(campoHtml("Precio", "$" + funcionesModel.getValueAt(i, 7)))
                    .append("<hr>");
        }
        if (cantidadFunciones == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay funciones programadas en esta sala."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionFila(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná un registro.");
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        for (int columna = 0; columna < tabla.getModel().getColumnCount(); columna++) {
            Object valor = tabla.getModel().getValueAt(fila, columna);
            descripcion.append(campoHtml(
                    tabla.getModel().getColumnName(columna),
                    valor == null || valor.toString().isBlank() ? "-" : valor
            ));
        }
        return descripcion.append("</body></html>").toString();
    }

    private void mostrarDetalle(String titulo, String descripcion) {
        mostrarDetalleConAccion(titulo, descripcion, null, null);
    }

    private void mostrarDetalleConAccion(String titulo, String descripcion, String accionTexto, Runnable accion) {
        JEditorPane area = crearVisorDetalle("");
        area.setText(descripcion);
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(440, 300));
        if (accion == null) {
            JOptionPane.showMessageDialog(this, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[] opciones = {"Cerrar", accionTexto};
        int opcion = JOptionPane.showOptionDialog(
                this, scroll, titulo, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opciones, opciones[0]
        );
        if (opcion == 1) {
            accion.run();
        }
    }

    private void mostrarSelectorEstadoSala() {
        JComboBox<String> estados = new JComboBox<>(new String[]{
                "DISPONIBLE", "LIMPIEZA", "CERRADA", "FUERA_DE_SERVICIO"
        });
        int opcion = JOptionPane.showConfirmDialog(
                this, estados, "Nuevo estado de la sala",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (opcion == JOptionPane.OK_OPTION) {
            cambiarEstadoSala(String.valueOf(estados.getSelectedItem()));
        }
    }

    private void cambiarEstadoSala(String nuevoEstado) {
        Integer id = idSeleccionado(salasTabla);
        if (id == null) {
            return;
        }
        ejecutar("Actualizando sala...",
                () -> apiClient.put("/salas/" + id + "/estado", Map.of("estado", nuevoEstado)),
                respuesta -> cargarSalas());
    }

    private void entregarConsumoSeleccionado() {
        int filaVista = consumosTabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarAviso("Seleccioná primero un consumo.");
            return;
        }
        int fila = consumosTabla.convertRowIndexToModel(filaVista);
        if (!"PENDIENTE".equals(String.valueOf(consumosModel.getValueAt(fila, 4)))) {
            mostrarAviso("Solo se puede entregar un consumo pendiente.");
            return;
        }
        operarConsumo("entregar");
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        JMenuItem salir = new JMenuItem("Salir");
        cerrar.addActionListener(event -> cerrarSesion());
        salir.addActionListener(event -> salirAplicacion());
        archivo.add(cerrar);
        archivo.addSeparator();
        archivo.add(salir);

        JMenu operacion = new JMenu("Operación");
        for (int i = 0; i < pestanias.getTabCount(); i++) {
            operacion.add(itemMenu(pestanias.getTitleAt(i)));
        }
        operacion.addSeparator();
        JMenuItem recargar = new JMenuItem("Recargar datos");
        recargar.addActionListener(event -> cargarTodo());
        operacion.add(recargar);

        barra.add(archivo);
        barra.add(operacion);
        return barra;
    }

    private JMenuItem itemMenu(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(event -> seleccionarPestania(texto));
        return item;
    }

    private void seleccionarPestania(String titulo) {
        for (int i = 0; i < pestanias.getTabCount(); i++) {
            if (titulo.equals(pestanias.getTitleAt(i))) {
                pestanias.setSelectedIndex(i);
                return;
            }
        }
    }

    private int indicePestania(String titulo) {
        for (int i = 0; i < pestanias.getTabCount(); i++) {
            if (titulo.equals(pestanias.getTitleAt(i))) {
                return i;
            }
        }
        return pestanias.getTabCount() - 1;
    }

    private void cargarTodo() {
        if (administracionPanel != null) {
            administracionPanel.recargar();
        }
        cargarFunciones();
        if (esDuenio() || esEmpleado()) {
            cargarSalas();
            cargarButacas();
        }
        if (esDuenio() || esEmpleado()) {
            cargarConsumos();
        }
        if (esDuenio()) {
            cargarEmpleados();
        }
    }

    private void cargarSalas() {
        ejecutar("Cargando salas...", () -> apiClient.get("/salas"), respuesta -> {
            Integer salaAnterior = salaSeleccionadaId();
            limpiar(salasModel);
            salaButacas.removeAllItems();
            respuesta.forEach(sala -> salasModel.addRow(new Object[]{
                    sala.path("id").asInt(), sala.path("nombre").asText(),
                    sala.path("capacidad").asInt(), sala.path("estado").asText("DISPONIBLE")
            }));
            respuesta.forEach(sala -> salaButacas.addItem(new SalaOpcion(
                    sala.path("id").asInt(), sala.path("nombre").asText()
            )));
            seleccionarSala(salaAnterior);
            actualizarMapaButacas();
        });
    }

    private void cargarFunciones() {
        ejecutar("Cargando funciones...", () -> apiClient.get("/funciones"), respuesta -> {
            limpiar(funcionesModel);
            respuesta.forEach(funcion -> funcionesModel.addRow(new Object[]{
                    funcion.path("id").asInt(), funcion.path("fecha").asText(),
                    funcion.path("horario").asText(), funcion.path("peliculaTitulo").asText(),
                    funcion.path("salaNombre").asText(), nombreFormato(funcion.path("formato").asText()),
                    nombreIdioma(funcion.path("idioma").asText()), funcion.path("precioEntrada").asDouble()
            }));
        });
    }

    private void cargarButacas() {
        ejecutar("Cargando butacas...", () -> apiClient.get("/butacas"), respuesta -> {
            limpiar(butacasModel);
            butacasActuales = new ArrayList<>();
            respuesta.forEach(butaca -> {
                butacasActuales.add(butaca);
                butacasModel.addRow(new Object[]{
                        butaca.path("id").asInt(), butaca.path("butacaNombre").asText(),
                        butaca.path("estado").asText(), butaca.path("salaNombre").asText()
                });
            });
            actualizarMapaButacas();
        });
    }

    private void cargarConsumos() {
        ejecutar("Cargando consumos...", () -> apiClient.get("/items-consumo"), respuesta -> {
            limpiar(consumosModel);
            respuesta.forEach(item -> consumosModel.addRow(new Object[]{
                    item.path("id").asInt(), item.path("productoNombre").asText(),
                    item.path("cantidad").asInt(), textoNullable(item.get("ticketId")),
                    item.path("estado").asText(), item.path("subtotal").asDouble()
            }));
        });
    }

    private void cargarEmpleados() {
        ejecutar("Cargando empleados...", () -> apiClient.get("/empleados"), respuesta -> {
            limpiar(empleadosModel);
            respuesta.forEach(empleado -> empleadosModel.addRow(new Object[]{
                    empleado.path("id").asInt(), empleado.path("nombre").asText(),
                    empleado.path("apellido").asText(), empleado.path("usuario").asText(),
                    empleado.path("activo").asBoolean()
            }));
        });
    }

    private void cambiarEstadoButaca(String operacion) {
        if (butacaSeleccionadaId == null) {
            mostrarAviso("Seleccioná una butaca del plano.");
            return;
        }
        int id = butacaSeleccionadaId;
        ejecutar("Actualizando butaca...", () -> apiClient.put("/butacas/" + id + "/" + operacion, null),
                respuesta -> cargarButacas());
    }

    private void actualizarMapaButacas() {
        mapaButacas.removeAll();
        SalaOpcion sala = (SalaOpcion) salaButacas.getSelectedItem();
        if (sala == null) {
            mapaButacas.add(new JLabel("No hay salas cargadas.", JLabel.CENTER), BorderLayout.CENTER);
            mapaButacas.revalidate();
            mapaButacas.repaint();
            return;
        }

        List<JsonNode> butacasSala = butacasActuales.stream()
                .filter(butaca -> butaca.path("salaId").asInt() == sala.id())
                .sorted(Comparator
                        .comparing((JsonNode butaca) -> butaca.path("fila").asText())
                        .thenComparingInt(butaca -> butaca.path("numero").asInt()))
                .toList();

        if (butacasSala.stream().noneMatch(butaca -> butaca.path("id").asInt() == valorO0(butacaSeleccionadaId))) {
            butacaSeleccionadaId = null;
        }

        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        contenido.add(new PantallaPanel(), BorderLayout.NORTH);

        JPanel filasPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        List<String> filas = butacasSala.stream()
                .map(butaca -> butaca.path("fila").asText())
                .distinct()
                .toList();
        int maximoNumero = butacasSala.stream()
                .mapToInt(butaca -> butaca.path("numero").asInt())
                .max()
                .orElse(0);

        for (String fila : filas) {
            JPanel filaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            JLabel etiquetaFila = new JLabel(fila);
            etiquetaFila.setFont(etiquetaFila.getFont().deriveFont(Font.BOLD));
            etiquetaFila.setPreferredSize(new Dimension(24, 38));
            filaPanel.add(etiquetaFila);
            for (int numero = 1; numero <= maximoNumero; numero++) {
                JsonNode butaca = buscarButaca(butacasSala, fila, numero);
                if (butaca == null) {
                    filaPanel.add(Box.createRigidArea(new Dimension(58, 38)));
                } else {
                    filaPanel.add(crearBotonButaca(butaca));
                }
            }
            filasPanel.add(filaPanel);
        }

        if (butacasSala.isEmpty()) {
            filasPanel.add(new JLabel("Esta sala no tiene butacas registradas.", JLabel.CENTER));
        }

        contenido.add(filasPanel, BorderLayout.CENTER);
        contenido.add(crearLeyendaButacas(), BorderLayout.SOUTH);
        mapaButacas.add(contenido, BorderLayout.CENTER);
        actualizarResumenButaca();
        mapaButacas.revalidate();
        mapaButacas.repaint();
    }

    private JButton crearBotonButaca(JsonNode butaca) {
        String estadoButaca = butaca.path("estado").asText("DISPONIBLE");
        String nombreButaca = butaca.path("fila").asText() + butaca.path("numero").asInt();
        JButton boton = new JButton(nombreButaca);
        boton.setUI(new BasicButtonUI());
        boton.setPreferredSize(new Dimension(58, 38));
        boton.setMinimumSize(new Dimension(58, 38));
        boton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
        boton.setBackground(colorEstadoButaca(estadoButaca));
        boton.setForeground(Color.WHITE);
        boton.setToolTipText("Butaca " + nombreButaca
                + " - " + nombreEstadoButaca(estadoButaca));

        int id = butaca.path("id").asInt();
        if (butacaSeleccionadaId != null && butacaSeleccionadaId == id) {
            boton.setBorder(BorderFactory.createLineBorder(new Color(30, 90, 180), 3));
        }
        boton.addActionListener(event -> {
            butacaSeleccionadaId = id;
            actualizarMapaButacas();
        });
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    mostrarDetalle("Información de la butaca", descripcionButaca(butaca));
                }
            }
        });
        return boton;
    }

    private String descripcionButaca(JsonNode butaca) {
        String nombreButaca = butaca.path("fila").asText() + butaca.path("numero").asInt();
        return inicioHtml()
                + tituloHtml("BUTACA")
                + campoHtml("Sala", butaca.path("salaNombre").asText())
                + campoHtml("Ubicación", nombreButaca)
                + campoHtml("Fila", butaca.path("fila").asText())
                + campoHtml("Número", butaca.path("numero").asInt())
                + campoHtml("Estado", nombreEstadoButaca(butaca.path("estado").asText()))
                + "</body></html>";
    }

    private JPanel crearLeyendaButacas() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        leyenda.add(elementoLeyenda("Disponible", colorEstadoButaca("DISPONIBLE")));
        leyenda.add(elementoLeyenda("Ocupada", colorEstadoButaca("OCUPADA")));
        leyenda.add(elementoLeyenda("Fuera de servicio", colorEstadoButaca("FUERA_DE_SERVICIO")));
        return leyenda;
    }

    private JPanel elementoLeyenda(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel muestra = new JLabel("  ");
        muestra.setOpaque(true);
        muestra.setBackground(color);
        muestra.setBorder(BorderFactory.createLineBorder(color.darker()));
        item.add(muestra);
        item.add(new JLabel(texto));
        return item;
    }

    private void actualizarResumenButaca() {
        if (butacaSeleccionadaId == null) {
            resumenButacas.setText("Seleccioná una butaca para cambiar su estado.");
            return;
        }
        butacasActuales.stream()
                .filter(butaca -> butaca.path("id").asInt() == butacaSeleccionadaId)
                .findFirst()
                .ifPresent(butaca -> resumenButacas.setText(
                        "Seleccionada: " + butaca.path("butacaNombre").asText()
                                + " (" + nombreEstadoButaca(butaca.path("estado").asText()) + ")"
                ));
    }

    private JsonNode buscarButaca(List<JsonNode> butacas, String fila, int numero) {
        return butacas.stream()
                .filter(butaca -> butaca.path("fila").asText().equals(fila)
                        && butaca.path("numero").asInt() == numero)
                .findFirst()
                .orElse(null);
    }

    private Integer salaSeleccionadaId() {
        SalaOpcion sala = (SalaOpcion) salaButacas.getSelectedItem();
        return sala == null ? null : sala.id();
    }

    private void seleccionarSala(Integer salaId) {
        if (salaId == null) {
            return;
        }
        for (int i = 0; i < salaButacas.getItemCount(); i++) {
            if (salaButacas.getItemAt(i).id() == salaId) {
                salaButacas.setSelectedIndex(i);
                return;
            }
        }
    }

    private int valorO0(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private Color colorEstadoButaca(String estadoButaca) {
        return switch (estadoButaca) {
            case "OCUPADA", "BLOQUEADA" -> new Color(105, 110, 118);
            case "FUERA_DE_SERVICIO" -> new Color(190, 52, 52);
            default -> new Color(46, 145, 82);
        };
    }

    private String nombreEstadoButaca(String estadoButaca) {
        return switch (estadoButaca) {
            case "OCUPADA" -> "Ocupada";
            case "BLOQUEADA" -> "No disponible";
            case "FUERA_DE_SERVICIO" -> "Fuera de servicio";
            default -> "Disponible";
        };
    }

    private static String nombreFormato(String formato) {
        return switch (formato) {
            case "DOS_D" -> "2D";
            case "TRES_D" -> "3D";
            default -> formato;
        };
    }

    private static String nombreIdioma(String idioma) {
        return "ESPANIOL".equals(idioma) ? "ESPAÑOL" : idioma;
    }

    private void buscarTicket() {
        String codigo = codigoQr.getText().trim();
        if (codigo.isBlank()) {
            mostrarAviso("Ingresá el código del Ticket.");
            return;
        }
        String codigoCodificado = URLEncoder.encode(codigo, StandardCharsets.UTF_8).replace("+", "%20");
        ejecutar("Buscando ticket...", () -> apiClient.get("/tickets/qr/" + codigoCodificado), respuesta -> {
            ticketActualId = respuesta.path("id").asInt();
            detalleTicket.setText(formatearTicket(respuesta));
            detalleTicket.setCaretPosition(0);
        });
    }

    private void operarTicket(String operacion) {
        if (ticketActualId == null) {
            mostrarAviso("Primero buscá un Ticket por su QR.");
            return;
        }
        ejecutar("Procesando ticket...",
                () -> apiClient.put("/tickets/" + ticketActualId + "/" + operacion, null),
                respuesta -> detalleTicket.setText(formatearTicket(respuesta)));
    }

    private void operarConsumo(String operacion) {
        Integer id = idSeleccionado(consumosTabla);
        if (id == null) {
            return;
        }
        ejecutar("Actualizando consumo...",
                () -> apiClient.put("/items-consumo/" + id + "/" + operacion, null),
                respuesta -> cargarConsumos());
    }

    private void cambiarEstadoEmpleado(String operacion) {
        Integer id = idSeleccionado(empleadosTabla);
        if (id == null) {
            return;
        }
        ejecutar("Actualizando empleado...",
                () -> apiClient.put("/empleados/" + id + "/" + operacion, null),
                respuesta -> cargarEmpleados());
    }

    private void mostrarFormularioHorario() {
        int filaVista = funcionesTabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarAviso("Seleccioná primero una función.");
            return;
        }
        int fila = funcionesTabla.convertRowIndexToModel(filaVista);
        int funcionId = ((Number) funcionesModel.getValueAt(fila, 0)).intValue();
        JTextField fecha = new JTextField(String.valueOf(funcionesModel.getValueAt(fila, 1)), 14);
        JTextField horario = new JTextField(String.valueOf(funcionesModel.getValueAt(fila, 2)), 10);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Fecha (AAAA-MM-DD)"));
        formulario.add(fecha);
        formulario.add(new JLabel("Horario (HH:mm)"));
        formulario.add(horario);

        int resultado = JOptionPane.showConfirmDialog(
                this, formulario, "Modificar horario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        Map<String, Object> cuerpo = Map.of(
                "fecha", fecha.getText().trim(),
                "horario", horario.getText().trim()
        );
        ejecutar("Actualizando horario...",
                () -> apiClient.put("/funciones/" + funcionId + "/horario", cuerpo),
                respuesta -> cargarFunciones());
    }

    private void mostrarFormularioEmpleado() {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField usuario = new JTextField();
        JPasswordField contrasenia = new JPasswordField();
        JPasswordField repetirContrasenia = new JPasswordField();
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Nombre"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido"));
        formulario.add(apellido);
        formulario.add(new JLabel("Usuario"));
        formulario.add(usuario);
        formulario.add(new JLabel("Contraseña"));
        formulario.add(contrasenia);
        formulario.add(new JLabel("Repetir contraseña"));
        formulario.add(repetirContrasenia);

        int resultado = JOptionPane.showConfirmDialog(this, formulario, "Nuevo empleado",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String clave = new String(contrasenia.getPassword());
        String claveRepetida = new String(repetirContrasenia.getPassword());
        if (!clave.equals(claveRepetida)) {
            mostrarAviso("Las contraseñas no coinciden.");
            return;
        }
        if (clave.length() < 6) {
            mostrarAviso("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        Map<String, Object> cuerpo = Map.of(
                "nombre", nombre.getText().trim(),
                "apellido", apellido.getText().trim(),
                "usuario", usuario.getText().trim(),
                "contrasenia", clave
        );
        ejecutar("Creando empleado...", () -> apiClient.post("/empleados", cuerpo),
                respuesta -> cargarEmpleados());
    }

    private String formatearTicket(JsonNode ticket) {
        StringBuilder texto = new StringBuilder();
        texto.append("TICKET #").append(ticket.path("id").asInt()).append('\n');
        texto.append("QR: ").append(ticket.path("codigoQR").asText()).append('\n');
        texto.append("Espectador: ").append(ticket.path("espectadorNombre").asText()).append('\n');
        texto.append("Pago: ").append(ticket.path("metodoDePagoResumen").asText("-")).append("\n\n");
        texto.append("ENTRADAS\n");
        ticket.path("entradas").forEach(entrada -> texto
                .append("- ").append(entrada.path("pelicula").asText())
                .append(" | ").append(entrada.path("fecha").asText())
                .append(' ').append(entrada.path("horario").asText())
                .append(" | Sala ").append(entrada.path("sala").asText())
                .append(" | Butaca ").append(entrada.path("butaca").asText())
                .append(" | ").append(entrada.path("estado").asText()).append('\n'));
        texto.append("\nCONSUMOS\n");
        if (ticket.path("itemsConsumo").isEmpty()) {
            texto.append("Sin consumos.\n");
        } else {
            ticket.path("itemsConsumo").forEach(item -> texto
                    .append("- ").append(item.path("producto").asText())
                    .append(" x").append(item.path("cantidad").asInt())
                    .append(" | ").append(item.path("estado").asText()).append('\n'));
        }
        texto.append("\nTOTAL: $").append(ticket.path("total").asDouble());
        return texto.toString();
    }

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Querés cerrar la sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar("Cerrando sesión...", () -> {
            apiClient.delete("/sesion");
            return null;
        }, respuesta -> {
            dispose();
            new LoginFrame(apiClient).setVisible(true);
        });
    }

    private void salirAplicacion() {
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Querés salir de la aplicación?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        dispose();
        System.exit(0);
    }

    private Integer idSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Seleccioná primero un registro de la tabla.");
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return ((Number) tabla.getModel().getValueAt(filaModelo, 0)).intValue();
    }

    private String textoNullable(JsonNode nodo) {
        return nodo == null || nodo.isNull() ? "-" : nodo.asText();
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        estado.setText(mensaje);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    T resultado = get();
                    alCompletar.accept(resultado);
                    estado.setText("Operación completada");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    estado.setText("Operación interrumpida");
                } catch (ExecutionException e) {
                    estado.setText("La operación falló");
                    JOptionPane.showMessageDialog(PrincipalFrame.this,
                            e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static JTable tabla(DefaultTableModel model) {
        JTable tabla = new JTable(model);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    private static void conectarFiltro(JTable tabla, JTextField campo) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) tabla.getModel());
        tabla.setRowSorter(sorter);
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void actualizar() {
                String texto = campo.getText().trim();
                sorter.setRowFilter(texto.isEmpty()
                        ? null
                        : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }
        });
    }

    private static JEditorPane crearVisorDetalle(String mensajeInicial) {
        JEditorPane visor = new JEditorPane("text/html", mensajeHtml(mensajeInicial));
        visor.setEditable(false);
        visor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        visor.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return visor;
    }

    private static String inicioHtml() {
        return "<html><body style='font-family:sans-serif;font-size:10px;margin:8px'>";
    }

    private static String mensajeHtml(String mensaje) {
        return inicioHtml() + escaparHtml(mensaje) + "</body></html>";
    }

    private static String campoHtml(String etiqueta, Object valor) {
        String contenido = escaparHtml(valor == null || valor.toString().isBlank() ? "-" : valor)
                .replace("\n", "<br>");
        return "<table width='100%' cellpadding='4' cellspacing='0'>"
                + "<tr><td width='38%'><b>" + escaparHtml(etiqueta) + ":</b></td>"
                + "<td>" + contenido + "</td></tr></table>";
    }

    private static String tituloHtml(String titulo) {
        return "<div align='center'><h2>" + escaparHtml(titulo) + "</h2></div><hr>";
    }

    private static String seccionHtml(String titulo) {
        return "<br><br><h3>" + escaparHtml(titulo) + "</h3><hr><br>";
    }

    private static String itemHtml(Object titulo) {
        return "<p><b>" + escaparHtml(titulo) + "</b></p>";
    }

    private static String mensajeSecundarioHtml(String mensaje) {
        return "<p><i>" + escaparHtml(mensaje) + "</i></p>";
    }

    private static String escaparHtml(Object valor) {
        return String.valueOf(valor)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String nombreRol(String rol) {
        return switch (rol) {
            case "DUENIO", "ADMIN" -> "Dueño";
            case "EMPLEADO" -> "Empleado";
            default -> "Personal";
        };
    }

    private boolean esDuenio() {
        return "DUENIO".equals(rol) || "ADMIN".equals(rol);
    }

    private boolean esEmpleado() {
        return "EMPLEADO".equals(rol);
    }

    private record SalaOpcion(int id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private static class PantallaPanel extends JPanel {
        private PantallaPanel() {
            setPreferredSize(new Dimension(760, 70));
            setMinimumSize(new Dimension(420, 70));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ancho = Math.max(240, getWidth() - 160);
            int x = (getWidth() - ancho) / 2;
            g2.setColor(new Color(62, 66, 72));
            g2.fillRoundRect(x, 12, ancho, 34, 22, 22);
            g2.setColor(new Color(225, 225, 225));
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            String texto = "PANTALLA";
            int textoX = (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2;
            g2.drawString(texto, textoX, 34);
            g2.dispose();
        }
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static void limpiar(DefaultTableModel model) {
        model.setRowCount(0);
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar() throws Exception;
    }
}
