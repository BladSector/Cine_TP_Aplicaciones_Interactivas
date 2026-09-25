package desktop;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class AdministracionPanel extends JPanel {
    private final ApiClient apiClient;
    private final JLabel estado = new JLabel("Cargando administración...");

    private final DefaultTableModel categoriasModel = modelo("ID", "Nombre");
    private final DefaultTableModel peliculasModel = modelo("ID", "Título", "Duración", "Categoría", "Descripción", "Portada");
    private final DefaultTableModel salasModel = modelo("ID", "Nombre", "Capacidad", "Estado");
    private final DefaultTableModel funcionesModel = modelo(
            "ID", "Fecha", "Horario", "Película ID", "Película", "Sala ID", "Sala", "Formato", "Idioma", "Precio"
    );
    private final DefaultTableModel productosModel = modelo("ID", "Nombre", "Precio", "Tipo", "Tamaño");

    private final JTable categoriasTabla = tabla(categoriasModel);
    private final JTable peliculasTabla = tabla(peliculasModel);
    private final JTable salasTabla = tabla(salasModel);
    private final JTable funcionesTabla = tabla(funcionesModel);
    private final JTable productosTabla = tabla(productosModel);

    private List<Opcion> categorias = new ArrayList<>();
    private List<Opcion> peliculas = new ArrayList<>();
    private List<Opcion> salas = new ArrayList<>();
    private final Map<Integer, String> portadasPeliculas = new HashMap<>();
    private Consumer<String> estadoExterno = mensaje -> {
    };

    public AdministracionPanel(ApiClient apiClient) {
        super(new BorderLayout());
        this.apiClient = apiClient;
        construirVista();
        cargarTodo();
    }

    public AdministracionPanel(ApiClient apiClient, JTabbedPane pestanias, Consumer<String> estadoExterno) {
        super(new BorderLayout());
        this.apiClient = apiClient;
        this.estadoExterno = estadoExterno == null ? mensaje -> {
        } : estadoExterno;
        agregarPestanias(pestanias);
    }

    public void recargar() {
        cargarTodo();
    }

    private void construirVista() {
        JTabbedPane pestanias = new JTabbedPane();
        agregarPestanias(pestanias);

        estado.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        add(pestanias, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }

    private void agregarPestanias(JTabbedPane pestanias) {
        pestanias.addTab("Categorías", panelCategorias());
        pestanias.addTab("Películas", panelPeliculas());
        pestanias.addTab("Salas", panelSalas());
        pestanias.addTab("Funciones", panelFunciones());
        pestanias.addTab("Confitería", panelProductos());
    }

    private JPanel panelCategorias() {
        Runnable editar = () -> formularioCategoria(true);
        Runnable eliminar = () -> eliminarSeleccionado(categoriasTabla, "/categorias/", "la categoría");
        return panelCrud("Categorías", categoriasTabla, editar, eliminar,
                boton("Nueva", event -> formularioCategoria(false)),
                boton("Editar", event -> editar.run()),
                boton("Eliminar", event -> eliminar.run()),
                boton("Recargar", event -> cargarTodo()));
    }

    private JPanel panelPeliculas() {
        Runnable editar = () -> formularioPelicula(true);
        Runnable eliminar = () -> eliminarSeleccionado(peliculasTabla, "/peliculas/", "la película");
        return panelCrud("Películas", peliculasTabla, editar, eliminar,
                boton("Nueva", event -> formularioPelicula(false)),
                boton("Editar", event -> editar.run()),
                boton("Eliminar", event -> eliminar.run()),
                boton("Recargar", event -> cargarTodo()));
    }

    private JPanel panelSalas() {
        Runnable editar = () -> formularioMatrizSala();
        Runnable eliminar = () -> eliminarSeleccionado(salasTabla, "/salas/", "la sala");
        return panelCrud("Salas", salasTabla, editar, eliminar,
                boton("Nueva sala", event -> formularioSalaNueva()),
                boton("Editar sala", event -> editar.run()),
                boton("Cambiar estado", event -> formularioEstadoSala()),
                boton("Eliminar", event -> eliminar.run()),
                boton("Recargar", event -> cargarTodo()));
    }

    private JPanel panelFunciones() {
        Runnable editar = () -> formularioFuncion(true);
        Runnable eliminar = () -> eliminarSeleccionado(funcionesTabla, "/funciones/", "la función");
        return panelCrud("Funciones", funcionesTabla, editar, eliminar,
                boton("Nueva función", event -> formularioFuncion(false)),
                boton("Editar", event -> editar.run()),
                boton("Eliminar", event -> eliminar.run()),
                boton("Recargar", event -> cargarTodo()));
    }

    private JPanel panelProductos() {
        Runnable editar = () -> formularioProducto(true);
        Runnable eliminar = () -> eliminarSeleccionado(productosTabla, "/productos-confiteria/", "el producto");
        return panelCrud("Productos de confitería", productosTabla, editar, eliminar,
                boton("Nuevo", event -> formularioProducto(false)),
                boton("Editar", event -> editar.run()),
                boton("Eliminar", event -> eliminar.run()),
                boton("Recargar", event -> cargarTodo()));
    }

    private JPanel panelCrud(String titulo, JTable tabla, Runnable editar, Runnable eliminar, JButton... botones) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 15f));
        JTextField filtro = new JTextField(24);
        filtro.putClientProperty("JTextField.placeholderText", "Buscar...");
        cabecera.add(etiqueta, BorderLayout.WEST);
        cabecera.add(filtro, BorderLayout.EAST);
        conectarFiltro(tabla, filtro);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton boton : botones) {
            acciones.add(boton);
        }
        JEditorPane detalle = crearAreaDetalle();
        tabla.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                actualizarDetalle(tabla, detalle);
            }
        });
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    int fila = tabla.rowAtPoint(event.getPoint());
                    if (fila >= 0) {
                        tabla.setRowSelectionInterval(fila, fila);
                        mostrarDetalleConAcciones(titulo, tabla, editar, eliminar);
                    }
                }
            }
        });

        JPanel tablaPanel = new JPanel(new BorderLayout(0, 8));
        tablaPanel.add(cabecera, BorderLayout.NORTH);
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel detallePanel = new JPanel(new BorderLayout(0, 8));
        detallePanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        detallePanel.setPreferredSize(new java.awt.Dimension(225, 0));
        JLabel tituloDetalle = new JLabel("Información");
        tituloDetalle.setFont(tituloDetalle.getFont().deriveFont(Font.BOLD));
        detallePanel.add(tituloDetalle, BorderLayout.NORTH);
        detallePanel.add(new JScrollPane(detalle), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablaPanel, detallePanel);
        division.setResizeWeight(0.82);
        division.setDividerLocation(0.82);
        division.setBorder(BorderFactory.createEmptyBorder());

        panel.add(division, BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JEditorPane crearAreaDetalle() {
        JEditorPane area = new JEditorPane("text/html", mensajeHtml("Seleccioná una fila para ver su información."));
        area.setEditable(false);
        area.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        return area;
    }

    private void actualizarDetalle(JTable tabla, JEditorPane detalle) {
        detalle.setText(descripcionSeleccion(tabla));
        detalle.setCaretPosition(0);
    }

    private String descripcionSeleccion(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná una fila para ver su información.");
        }
        if (tabla == categoriasTabla) {
            return descripcionCategoriaSeleccionada(filaVista);
        }
        if (tabla == peliculasTabla) {
            return descripcionPeliculaSeleccionada(filaVista);
        }
        if (tabla == salasTabla) {
            return descripcionSalaSeleccionada(filaVista);
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        for (int columna = 0; columna < tabla.getModel().getColumnCount(); columna++) {
            Object valor = tabla.getModel().getValueAt(filaModelo, columna);
            descripcion.append(campoHtml(
                    tabla.getModel().getColumnName(columna),
                    valor == null || valor.toString().isBlank() ? "-" : valor
            ));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionCategoriaSeleccionada(int filaVista) {
        int fila = categoriasTabla.convertRowIndexToModel(filaVista);
        String nombreCategoria = valor(categoriasModel, fila, 1);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("CATEGORÍA"))
                .append(campoHtml("Nombre", nombreCategoria))
                .append(seccionHtml("PELÍCULAS ASOCIADAS"));

        int cantidadPeliculas = 0;
        for (int i = 0; i < peliculasModel.getRowCount(); i++) {
            if (!nombreCategoria.equals(valor(peliculasModel, i, 3))) {
                continue;
            }
            cantidadPeliculas++;
            descripcion.append(itemHtml(valor(peliculasModel, i, 1)))
                    .append(campoHtml("ID", valor(peliculasModel, i, 0)))
                    .append(campoHtml("Duración", valor(peliculasModel, i, 2) + " min"))
                    .append("<hr>");
        }
        if (cantidadPeliculas == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay películas asociadas a esta categoría."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionPeliculaSeleccionada(int filaVista) {
        int fila = peliculasTabla.convertRowIndexToModel(filaVista);
        int peliculaId = id(peliculasModel, fila);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("PELÍCULA"))
                .append(campoHtml("Título", valor(peliculasModel, fila, 1)))
                .append(campoHtml("Descripción", valor(peliculasModel, fila, 4)))
                .append(campoHtml("Duración", valor(peliculasModel, fila, 2) + " min"))
                .append(campoHtml("Categoría", valor(peliculasModel, fila, 3)))
                .append(campoHtml("Portada", valor(peliculasModel, fila, 5)))
                .append(seccionHtml("FUNCIONES ASOCIADAS"));

        int cantidadFunciones = 0;
        for (int i = 0; i < funcionesModel.getRowCount(); i++) {
            if (numero(funcionesModel, i, 3) != peliculaId) {
                continue;
            }
            cantidadFunciones++;
            descripcion.append(itemHtml(valor(funcionesModel, i, 1) + " - " + valor(funcionesModel, i, 2)))
                    .append(campoHtml("Sala", valor(funcionesModel, i, 6)))
                    .append(campoHtml("Formato", valor(funcionesModel, i, 7)))
                    .append(campoHtml("Idioma", valor(funcionesModel, i, 8)))
                    .append(campoHtml("Precio", "$" + valor(funcionesModel, i, 9)))
                    .append("<hr>");
        }
        if (cantidadFunciones == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay funciones programadas para esta película."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private String descripcionSalaSeleccionada(int filaVista) {
        int fila = salasTabla.convertRowIndexToModel(filaVista);
        String nombreSala = valor(salasModel, fila, 1);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("SALA"))
                .append(campoHtml("Nombre", nombreSala))
                .append(campoHtml("Capacidad", valor(salasModel, fila, 2)))
                .append(campoHtml("Estado", valor(salasModel, fila, 3)))
                .append(seccionHtml("FUNCIONES PROGRAMADAS"));

        int cantidadFunciones = 0;
        for (int i = 0; i < funcionesModel.getRowCount(); i++) {
            if (!nombreSala.equals(valor(funcionesModel, i, 6))) {
                continue;
            }
            cantidadFunciones++;
            descripcion.append(itemHtml(valor(funcionesModel, i, 4)))
                    .append(campoHtml("Fecha", valor(funcionesModel, i, 1)))
                    .append(campoHtml("Horario", valor(funcionesModel, i, 2)))
                    .append(campoHtml("Formato", valor(funcionesModel, i, 7)))
                    .append(campoHtml("Idioma", valor(funcionesModel, i, 8)))
                    .append(campoHtml("Precio", "$" + valor(funcionesModel, i, 9)))
                    .append("<hr>");
        }
        if (cantidadFunciones == 0) {
            descripcion.append(mensajeSecundarioHtml("No hay funciones programadas en esta sala."));
        }
        return descripcion.append("</body></html>").toString();
    }

    private void mostrarDetalleConAcciones(String titulo, JTable tabla, Runnable editar, Runnable eliminar) {
        JEditorPane detalle = crearAreaDetalle();
        detalle.setText(descripcionSeleccion(tabla));
        detalle.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setPreferredSize(new java.awt.Dimension(480, 320));

        Object[] opciones = {"Cerrar", "Editar", "Eliminar"};
        int opcion = JOptionPane.showOptionDialog(
                this,
                scroll,
                "Información de " + titulo.toLowerCase(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        if (opcion == 1) {
            editar.run();
        } else if (opcion == 2) {
            eliminar.run();
        }
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

    private static String itemHtml(String titulo) {
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

    private void formularioCategoria(boolean edicion) {
        int fila = edicion ? filaSeleccionada(categoriasTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        JTextField nombre = new JTextField(edicion ? valor(categoriasModel, fila, 1) : "", 24);
        if (!confirmarFormulario("Categoría", formulario(new String[]{"Nombre"}, nombre))) {
            return;
        }
        Map<String, Object> cuerpo = Map.of("nombre", nombre.getText().trim());
        if (edicion) {
            int id = id(categoriasModel, fila);
            ejecutar("Actualizando categoría...", () -> apiClient.put("/categorias/" + id, cuerpo), respuesta -> cargarTodo());
        } else {
            ejecutar("Creando categoría...", () -> apiClient.post("/categorias", cuerpo), respuesta -> cargarTodo());
        }
    }

    private void formularioPelicula(boolean edicion) {
        int fila = edicion ? filaSeleccionada(peliculasTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        if (categorias.isEmpty()) {
            aviso("Primero creá una Categoría.");
            return;
        }

        JTextField titulo = new JTextField(edicion ? valor(peliculasModel, fila, 1) : "", 24);
        JSpinner duracion = new JSpinner(new SpinnerNumberModel(
                edicion ? numero(peliculasModel, fila, 2) : 100, 1, 600, 1));
        JTextArea descripcion = new JTextArea(edicion ? valor(peliculasModel, fila, 4) : "", 3, 24);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(descripcion);
        scrollDescripcion.setPreferredSize(new Dimension(320, 95));
        int peliculaId = edicion ? id(peliculasModel, fila) : 0;
        String[] portada = {edicion ? portadasPeliculas.getOrDefault(peliculaId, "") : ""};
        JLabel vistaPrevia = new JLabel("Sin imagen", JLabel.CENTER);
        vistaPrevia.setPreferredSize(new Dimension(90, 120));
        vistaPrevia.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        actualizarVistaPrevia(vistaPrevia, portada[0]);
        JButton elegirImagen = new JButton("Elegir imagen...");
        elegirImagen.addActionListener(event -> seleccionarImagenLocal(portada, vistaPrevia));
        JPanel controlImagen = new JPanel(new BorderLayout(6, 6));
        controlImagen.add(vistaPrevia, BorderLayout.CENTER);
        controlImagen.add(elegirImagen, BorderLayout.SOUTH);
        JComboBox<Opcion> categoria = new JComboBox<>(categorias.toArray(Opcion[]::new));
        if (edicion) {
            seleccionarPorNombre(categoria, valor(peliculasModel, fila, 3));
        }

        JPanel panel = formulario(
                new String[]{"Título", "Duración (min)", "Categoría", "Descripción", "Imagen de portada"},
                titulo, duracion, categoria, scrollDescripcion, controlImagen
        );
        if (!confirmarFormulario("Película", panel)) {
            return;
        }

        Opcion categoriaElegida = (Opcion) categoria.getSelectedItem();
        Map<String, Object> cuerpo = Map.of(
                "titulo", titulo.getText().trim(),
                "duracion", duracion.getValue(),
                "descripcion", descripcion.getText().trim(),
                "portadaUrl", portada[0],
                "categoriaId", categoriaElegida.id()
        );
        guardarOActualizar(edicion, peliculasModel, fila, "/peliculas", cuerpo, "película");
    }

    private void seleccionarImagenLocal(String[] portada, JLabel vistaPrevia) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Elegir imagen de portada");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes JPG, PNG o WEBP", "jpg", "jpeg", "png", "webp"));

        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        try {
            byte[] contenido = Files.readAllBytes(archivo.toPath());
            if (contenido.length > 5 * 1024 * 1024) {
                aviso("La imagen no puede superar los 5 MB.");
                return;
            }

            String mime = Files.probeContentType(archivo.toPath());
            if (mime == null || !mime.startsWith("image/")) {
                mime = mimePorExtension(archivo.getName());
            }
            if (mime == null) {
                aviso("El archivo elegido no es una imagen JPG, PNG o WEBP.");
                return;
            }

            portada[0] = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(contenido);
            actualizarVistaPrevia(vistaPrevia, portada[0]);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer la imagen: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mimePorExtension(String nombre) {
        String minuscula = nombre.toLowerCase();
        if (minuscula.endsWith(".jpg") || minuscula.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (minuscula.endsWith(".png")) {
            return "image/png";
        }
        if (minuscula.endsWith(".webp")) {
            return "image/webp";
        }
        return null;
    }

    private void actualizarVistaPrevia(JLabel vistaPrevia, String portada) {
        vistaPrevia.setIcon(null);
        if (portada == null || portada.isBlank()) {
            vistaPrevia.setText("Sin imagen");
            return;
        }
        if (!portada.startsWith("data:image/") || !portada.contains(",")) {
            vistaPrevia.setText("Imagen actual");
            return;
        }

        try {
            String base64 = portada.substring(portada.indexOf(',') + 1);
            ImageIcon original = new ImageIcon(Base64.getDecoder().decode(base64));
            if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
                throw new IllegalArgumentException("Imagen inválida");
            }

            double escala = Math.min(82.0 / original.getIconWidth(), 112.0 / original.getIconHeight());
            int ancho = Math.max(1, (int) (original.getIconWidth() * escala));
            int alto = Math.max(1, (int) (original.getIconHeight() * escala));
            Image imagen = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            vistaPrevia.setText("");
            vistaPrevia.setIcon(new ImageIcon(imagen));
        } catch (IllegalArgumentException e) {
            vistaPrevia.setText("Imagen inválida");
        }
    }

    private void formularioSalaNueva() {
        EditorDistribucionSala editor = EditorDistribucionSala.nuevo();
        if (!confirmarEditorSala("Nueva sala", editor)) {
            return;
        }
        Map<String, Object> cuerpo = Map.of(
                "nombre", editor.getNombre(),
                "butacas", editor.getUbicaciones()
        );
        ejecutar("Creando sala y butacas...",
                () -> apiClient.post("/salas/con-matriz", cuerpo), respuesta -> cargarTodo());
    }

    private void formularioMatrizSala() {
        int filaSala = filaSeleccionada(salasTabla);
        if (filaSala < 0) {
            return;
        }
        int salaId = id(salasModel, filaSala);
        String nombreSala = valor(salasModel, filaSala, 1);
        ejecutar("Cargando distribución...", () -> apiClient.get("/butacas"),
                respuesta -> editarDistribucionSala(salaId, nombreSala, respuesta));
    }

    private void editarDistribucionSala(int salaId, String nombreSala, JsonNode respuesta) {
        List<JsonNode> butacasSala = new ArrayList<>();
        respuesta.forEach(butaca -> {
            if (butaca.path("salaId").asInt() == salaId) {
                butacasSala.add(butaca);
            }
        });
        EditorDistribucionSala editor = EditorDistribucionSala.desde(nombreSala, butacasSala);
        if (!confirmarEditorSala("Editar " + nombreSala, editor)) {
            return;
        }
        Map<String, Object> cuerpo = Map.of(
                "nombre", editor.getNombre(),
                "butacas", editor.getUbicaciones()
        );
        ejecutar("Actualizando distribución...",
                () -> apiClient.put("/salas/" + salaId + "/distribucion", cuerpo), respuestaApi -> cargarTodo());
    }

    private boolean confirmarEditorSala(String titulo, EditorDistribucionSala editor) {
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(860, 560));
        int opcion = JOptionPane.showConfirmDialog(
                this, scroll, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (opcion != JOptionPane.OK_OPTION) {
            return false;
        }
        if (editor.getNombre().isBlank()) {
            aviso("El nombre de la Sala es obligatorio.");
            return false;
        }
        if (editor.getUbicaciones().isEmpty()) {
            aviso("La Sala debe tener al menos una Butaca.");
            return false;
        }
        return true;
    }

    private void formularioEstadoSala() {
        int fila = filaSeleccionada(salasTabla);
        if (fila < 0) {
            return;
        }
        JComboBox<String> estadoSala = new JComboBox<>(new String[]{
                "DISPONIBLE", "LIMPIEZA", "CERRADA", "FUERA_DE_SERVICIO"
        });
        estadoSala.setSelectedItem(valor(salasModel, fila, 3));
        if (JOptionPane.showConfirmDialog(this, estadoSala, "Nuevo estado de la sala",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }

        int salaId = id(salasModel, fila);
        Map<String, Object> cuerpo = Map.of("estado", estadoSala.getSelectedItem());
        ejecutar("Actualizando estado de la sala...",
                () -> apiClient.put("/salas/" + salaId + "/estado", cuerpo), respuesta -> cargarTodo());
    }

    private void formularioFuncion(boolean edicion) {
        int fila = edicion ? filaSeleccionada(funcionesTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        if (peliculas.isEmpty() || salas.isEmpty()) {
            aviso("Primero deben existir Películas y Salas.");
            return;
        }

        JTextField fecha = new JTextField(edicion ? valor(funcionesModel, fila, 1) : LocalDate.now().toString());
        JTextField horario = new JTextField(edicion ? valor(funcionesModel, fila, 2) : "20:30");
        JComboBox<Opcion> pelicula = new JComboBox<>(peliculas.toArray(Opcion[]::new));
        JComboBox<Opcion> sala = new JComboBox<>(salas.toArray(Opcion[]::new));
        JComboBox<String> formato = new JComboBox<>(new String[]{"2D", "3D"});
        JComboBox<String> idioma = new JComboBox<>(new String[]{"ESPAÑOL", "SUBTITULADA"});
        JSpinner precio = new JSpinner(new SpinnerNumberModel(
                edicion ? decimal(funcionesModel, fila, 9) : 8000.0, 0.01, 10000000.0, 100.0));

        if (edicion) {
            seleccionarPorId(pelicula, numero(funcionesModel, fila, 3));
            seleccionarPorId(sala, numero(funcionesModel, fila, 5));
            formato.setSelectedItem(nombreFormato(valor(funcionesModel, fila, 7)));
            idioma.setSelectedItem(nombreIdioma(valor(funcionesModel, fila, 8)));
        }

        JPanel panel = formulario(
                new String[]{"Fecha (AAAA-MM-DD)", "Horario (HH:mm)", "Película", "Sala", "Formato", "Idioma", "Precio"},
                fecha, horario, pelicula, sala, formato, idioma, precio
        );
        if (!confirmarFormulario("Función", panel)) {
            return;
        }

        Map<String, Object> cuerpo = Map.of(
                "fecha", fecha.getText().trim(),
                "horario", horario.getText().trim(),
                "peliculaId", ((Opcion) pelicula.getSelectedItem()).id(),
                "salaId", ((Opcion) sala.getSelectedItem()).id(),
                "formato", codigoFormato(String.valueOf(formato.getSelectedItem())),
                "idioma", codigoIdioma(String.valueOf(idioma.getSelectedItem())),
                "precioEntrada", precio.getValue()
        );
        guardarOActualizar(edicion, funcionesModel, fila, "/funciones", cuerpo, "función");
    }

    private void formularioProducto(boolean edicion) {
        int fila = edicion ? filaSeleccionada(productosTabla) : -1;
        if (edicion && fila < 0) {
            return;
        }
        JTextField nombre = new JTextField(edicion ? valor(productosModel, fila, 1) : "");
        JSpinner precio = new JSpinner(new SpinnerNumberModel(
                edicion ? decimal(productosModel, fila, 2) : 1000.0, 0.01, 10000000.0, 100.0));
        JComboBox<String> tipo = new JComboBox<>(new String[]{"POCHOCLOS", "BEBIDA", "DULCE", "COMBO"});
        JComboBox<String> tamano = new JComboBox<>(new String[]{"CHICO", "MEDIANO", "GRANDE", "UNICO"});
        if (edicion) {
            tipo.setSelectedItem(valor(productosModel, fila, 3));
            tamano.setSelectedItem(valor(productosModel, fila, 4));
        }
        if (!confirmarFormulario("Producto de confitería", formulario(
                new String[]{"Nombre", "Precio", "Tipo", "Tamaño"}, nombre, precio, tipo, tamano))) {
            return;
        }
        Map<String, Object> cuerpo = Map.of(
                "nombre", nombre.getText().trim(),
                "precio", precio.getValue(),
                "tipo", tipo.getSelectedItem(),
                "tamano", tamano.getSelectedItem()
        );
        guardarOActualizar(edicion, productosModel, fila, "/productos-confiteria", cuerpo, "producto");
    }

    private void guardarOActualizar(boolean edicion, DefaultTableModel model, int fila,
                                    String ruta, Map<String, Object> cuerpo, String entidad) {
        if (edicion) {
            int id = id(model, fila);
            ejecutar("Actualizando " + entidad + "...", () -> apiClient.put(ruta + "/" + id, cuerpo), respuesta -> cargarTodo());
        } else {
            ejecutar("Creando " + entidad + "...", () -> apiClient.post(ruta, cuerpo), respuesta -> cargarTodo());
        }
    }

    private void eliminarSeleccionado(JTable tabla, String ruta, String entidad) {
        int fila = filaSeleccionada(tabla);
        if (fila < 0) {
            return;
        }
        int id = ((Number) tabla.getModel().getValueAt(fila, 0)).intValue();
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que querés eliminar " + entidad + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        ejecutar("Eliminando...", () -> {
            apiClient.delete(ruta + id);
            return null;
        }, ignorado -> cargarTodo());
    }

    private void cargarTodo() {
        ejecutar("Cargando datos administrativos...", () -> new DatosAdministracion(
                apiClient.get("/categorias"),
                apiClient.get("/peliculas"),
                apiClient.get("/salas"),
                apiClient.get("/funciones"),
                apiClient.get("/productos-confiteria")
        ), this::aplicarDatos);
    }

    private void aplicarDatos(DatosAdministracion datos) {
        limpiar(categoriasModel);
        categorias = new ArrayList<>();
        datos.categorias().forEach(item -> {
            categoriasModel.addRow(new Object[]{item.path("id").asInt(), item.path("nombre").asText()});
            categorias.add(new Opcion(item.path("id").asInt(), item.path("nombre").asText()));
        });

        limpiar(peliculasModel);
        peliculas = new ArrayList<>();
        portadasPeliculas.clear();
        datos.peliculas().forEach(item -> {
            int peliculaId = item.path("id").asInt();
            String portada = item.path("portadaUrl").asText("");
            portadasPeliculas.put(peliculaId, portada);
            peliculasModel.addRow(new Object[]{
                    peliculaId, item.path("titulo").asText(), item.path("duracion").asInt(),
                    item.path("categoriaNombre").asText(), item.path("descripcion").asText(),
                    portada.isBlank() ? "Sin portada" : "Cargada"
            });
            peliculas.add(new Opcion(peliculaId, item.path("titulo").asText()));
        });

        limpiar(salasModel);
        salas = new ArrayList<>();
        datos.salas().forEach(item -> {
            salasModel.addRow(new Object[]{
                    item.path("id").asInt(), item.path("nombre").asText(),
                    item.path("capacidad").asInt(), item.path("estado").asText("DISPONIBLE")
            });
            salas.add(new Opcion(item.path("id").asInt(), item.path("nombre").asText()));
        });

        limpiar(funcionesModel);
        datos.funciones().forEach(item -> funcionesModel.addRow(new Object[]{
                item.path("id").asInt(), item.path("fecha").asText(), item.path("horario").asText(),
                item.path("peliculaId").asInt(), item.path("peliculaTitulo").asText(),
                item.path("salaId").asInt(), item.path("salaNombre").asText(),
                nombreFormato(item.path("formato").asText()), nombreIdioma(item.path("idioma").asText()),
                item.path("precioEntrada").asDouble()
        }));

        limpiar(productosModel);
        datos.productos().forEach(item -> productosModel.addRow(new Object[]{
                item.path("id").asInt(), item.path("nombre").asText(), item.path("precio").asDouble(),
                item.path("tipo").asText(), item.path("tamano").asText()
        }));
    }

    private JPanel formulario(String[] etiquetas, java.awt.Component... componentes) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        for (int i = 0; i < etiquetas.length; i++) {
            if (componentes[i] instanceof JTextField campo && campo.getColumns() == 0) {
                campo.setColumns(22);
            }

            GridBagConstraints etiqueta = new GridBagConstraints();
            etiqueta.gridx = 0;
            etiqueta.gridy = i;
            etiqueta.anchor = GridBagConstraints.LINE_END;
            etiqueta.insets = new Insets(4, 4, 4, 8);
            panel.add(new JLabel(etiquetas[i]), etiqueta);

            GridBagConstraints control = new GridBagConstraints();
            control.gridx = 1;
            control.gridy = i;
            control.weightx = 1;
            control.fill = GridBagConstraints.HORIZONTAL;
            control.anchor = GridBagConstraints.LINE_START;
            control.insets = new Insets(4, 0, 4, 4);
            panel.add(componentes[i], control);
        }
        return panel;
    }

    private boolean confirmarFormulario(String titulo, JPanel panel) {
        return JOptionPane.showConfirmDialog(this, panel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private int filaSeleccionada(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            aviso("Seleccioná primero un registro.");
            return -1;
        }
        return tabla.convertRowIndexToModel(filaVista);
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        actualizarEstado(mensaje);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    alCompletar.accept(get());
                    actualizarEstado("Operación completada");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    actualizarEstado("Operación interrumpida");
                } catch (ExecutionException e) {
                    actualizarEstado("La operación falló");
                    JOptionPane.showMessageDialog(AdministracionPanel.this,
                            e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void actualizarEstado(String mensaje) {
        estado.setText(mensaje);
        estadoExterno.accept(mensaje);
    }

    private JButton boton(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(accion);
        return boton;
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

    private static int id(DefaultTableModel model, int fila) {
        return ((Number) model.getValueAt(fila, 0)).intValue();
    }

    private static int numero(DefaultTableModel model, int fila, int columna) {
        return ((Number) model.getValueAt(fila, columna)).intValue();
    }

    private static double decimal(DefaultTableModel model, int fila, int columna) {
        return ((Number) model.getValueAt(fila, columna)).doubleValue();
    }

    private static String valor(DefaultTableModel model, int fila, int columna) {
        Object valor = model.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString();
    }

    private static void seleccionarPorNombre(JComboBox<Opcion> combo, String nombre) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).nombre().equals(nombre)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static void seleccionarPorId(JComboBox<Opcion> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private static String nombreFormato(String formato) {
        return switch (formato) {
            case "DOS_D" -> "2D";
            case "TRES_D" -> "3D";
            default -> formato;
        };
    }

    private static String codigoFormato(String formato) {
        return "3D".equals(formato) ? "TRES_D" : "DOS_D";
    }

    private static String nombreIdioma(String idioma) {
        return "ESPANIOL".equals(idioma) ? "ESPAÑOL" : idioma;
    }

    private static String codigoIdioma(String idioma) {
        return "ESPAÑOL".equals(idioma) ? "ESPANIOL" : idioma;
    }

    private static class EditorDistribucionSala extends JPanel {
        private static final Color BUTACA_ACTIVA = new Color(35, 142, 74);
        private static final Color BUTACA_INACTIVA = new Color(105, 111, 121);

        private final JTextField nombre = new JTextField(22);
        private final JSpinner filas;
        private final JSpinner columnas;
        private final JPanel mapa = new JPanel(new BorderLayout(0, 16));
        private final Set<String> activas = new HashSet<>();
        private int filasAnteriores;
        private int columnasAnteriores;

        private EditorDistribucionSala(String nombreSala, int cantidadFilas, int cantidadColumnas,
                                       Set<String> posicionesActivas) {
            super(new BorderLayout(0, 16));
            nombre.setText(nombreSala);
            filas = new JSpinner(new SpinnerNumberModel(cantidadFilas, 1, 26, 1));
            columnas = new JSpinner(new SpinnerNumberModel(cantidadColumnas, 1, 40, 1));
            filasAnteriores = cantidadFilas;
            columnasAnteriores = cantidadColumnas;
            activas.addAll(posicionesActivas);

            JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
            controles.add(new JLabel("Nombre"));
            controles.add(nombre);
            controles.add(new JLabel("Filas"));
            controles.add(filas);
            controles.add(new JLabel("Butacas por fila"));
            controles.add(columnas);

            filas.addChangeListener(event -> cambiarDimensiones());
            columnas.addChangeListener(event -> cambiarDimensiones());

            add(controles, BorderLayout.NORTH);
            add(mapa, BorderLayout.CENTER);
            dibujarMapa();
        }

        static EditorDistribucionSala nuevo() {
            int filas = 5;
            int columnas = 10;
            Set<String> posiciones = new HashSet<>();
            for (int fila = 0; fila < filas; fila++) {
                for (int numero = 1; numero <= columnas; numero++) {
                    posiciones.add(posicion(fila, numero));
                }
            }
            return new EditorDistribucionSala("", filas, columnas, posiciones);
        }

        static EditorDistribucionSala desde(String nombre, List<JsonNode> butacas) {
            int cantidadFilas = 1;
            int cantidadColumnas = 1;
            Set<String> posiciones = new HashSet<>();

            for (JsonNode butaca : butacas) {
                String fila = butaca.path("fila").asText("A").trim().toUpperCase();
                int indiceFila = Math.max(0, fila.charAt(0) - 'A');
                int numero = Math.max(1, butaca.path("numero").asInt(1));
                cantidadFilas = Math.max(cantidadFilas, indiceFila + 1);
                cantidadColumnas = Math.max(cantidadColumnas, numero);
                posiciones.add(posicion(indiceFila, numero));
            }

            return new EditorDistribucionSala(nombre, cantidadFilas, cantidadColumnas, posiciones);
        }

        String getNombre() {
            return nombre.getText().trim();
        }

        List<Map<String, Object>> getUbicaciones() {
            int cantidadFilas = (Integer) filas.getValue();
            int cantidadColumnas = (Integer) columnas.getValue();
            return activas.stream()
                    .map(EditorDistribucionSala::leerPosicion)
                    .filter(posicion -> posicion.fila() < cantidadFilas && posicion.numero() <= cantidadColumnas)
                    .sorted(Comparator.comparingInt(PosicionSala::fila).thenComparingInt(PosicionSala::numero))
                    .map(posicion -> Map.<String, Object>of(
                            "fila", String.valueOf((char) ('A' + posicion.fila())),
                            "numero", posicion.numero()))
                    .toList();
        }

        private void cambiarDimensiones() {
            int nuevasFilas = (Integer) filas.getValue();
            int nuevasColumnas = (Integer) columnas.getValue();

            activas.removeIf(clave -> {
                PosicionSala posicion = leerPosicion(clave);
                return posicion.fila() >= nuevasFilas || posicion.numero() > nuevasColumnas;
            });

            for (int fila = 0; fila < nuevasFilas; fila++) {
                for (int numero = 1; numero <= nuevasColumnas; numero++) {
                    if (fila >= filasAnteriores || numero > columnasAnteriores) {
                        activas.add(posicion(fila, numero));
                    }
                }
            }

            filasAnteriores = nuevasFilas;
            columnasAnteriores = nuevasColumnas;
            dibujarMapa();
        }

        private void dibujarMapa() {
            mapa.removeAll();
            int cantidadFilas = (Integer) filas.getValue();
            int cantidadColumnas = (Integer) columnas.getValue();

            JLabel pantalla = new JLabel("PANTALLA", JLabel.CENTER);
            pantalla.setOpaque(true);
            pantalla.setBackground(new Color(39, 43, 50));
            pantalla.setForeground(Color.WHITE);
            pantalla.setFont(pantalla.getFont().deriveFont(Font.BOLD, 15f));
            pantalla.setPreferredSize(new Dimension(Math.max(520, cantidadColumnas * 55), 42));
            pantalla.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

            JPanel grilla = new JPanel(new GridBagLayout());
            GridBagConstraints restricciones = new GridBagConstraints();
            restricciones.insets = new Insets(3, 3, 3, 3);
            restricciones.fill = GridBagConstraints.BOTH;

            for (int fila = 0; fila < cantidadFilas; fila++) {
                restricciones.gridx = 0;
                restricciones.gridy = fila;
                restricciones.weightx = 0;
                JLabel etiquetaFila = new JLabel(String.valueOf((char) ('A' + fila)), JLabel.CENTER);
                etiquetaFila.setPreferredSize(new Dimension(28, 34));
                grilla.add(etiquetaFila, restricciones);

                for (int numero = 1; numero <= cantidadColumnas; numero++) {
                    String clave = posicion(fila, numero);
                    JButton butaca = new JButton(String.valueOf(numero));
                    butaca.setPreferredSize(new Dimension(48, 34));
                    butaca.setMargin(new Insets(2, 4, 2, 4));
                    butaca.setFocusPainted(false);
                    pintarButaca(butaca, activas.contains(clave));
                    butaca.addActionListener(event -> {
                        if (!activas.remove(clave)) {
                            activas.add(clave);
                        }
                        pintarButaca(butaca, activas.contains(clave));
                    });

                    restricciones.gridx = numero;
                    restricciones.weightx = 1;
                    grilla.add(butaca, restricciones);
                }
            }

            JPanel contenido = new JPanel(new BorderLayout(0, 16));
            contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 16, 12));
            contenido.add(pantalla, BorderLayout.NORTH);
            contenido.add(grilla, BorderLayout.CENTER);
            mapa.add(contenido, BorderLayout.NORTH);
            mapa.revalidate();
            mapa.repaint();
        }

        private static void pintarButaca(JButton butaca, boolean activa) {
            butaca.setOpaque(true);
            butaca.setBackground(activa ? BUTACA_ACTIVA : BUTACA_INACTIVA);
            butaca.setForeground(Color.WHITE);
            butaca.setToolTipText(activa ? "Butaca incluida" : "Espacio sin butaca");
        }

        private static String posicion(int fila, int numero) {
            return fila + "-" + numero;
        }

        private static PosicionSala leerPosicion(String clave) {
            String[] partes = clave.split("-", 2);
            return new PosicionSala(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
        }

        private record PosicionSala(int fila, int numero) {
        }
    }

    private record Opcion(int id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private record DatosAdministracion(JsonNode categorias, JsonNode peliculas, JsonNode salas,
                                       JsonNode funciones, JsonNode productos) {
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar() throws Exception;
    }
}
