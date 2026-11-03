package desktop.comun.modulos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.databind.JsonNode;

import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.comun.api.PersonalApi;
import desktop.comun.api.SalasApi;
import desktop.comun.componentes.PanelConTabla;
import desktop.comun.dialogos.EstadoButacaDialog;
import desktop.comun.dialogos.EstadoSalaDialog;
import desktop.comun.paneles.ButacasPanel;
import desktop.empleado.paneles.FuncionesPanel;
import desktop.empleado.paneles.SalasPanel;

import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.tabla;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.itemHtml;
import static desktop.comun.util.DetalleHtml.mensajeHtml;
import static desktop.comun.util.DetalleHtml.mensajeSecundarioHtml;
import static desktop.comun.util.DetalleHtml.seccionHtml;
import static desktop.comun.util.DetalleHtml.tituloHtml;
import static desktop.comun.util.FormatoSwing.nombreEstadoButaca;
import static desktop.comun.util.FormatoSwing.nombreEstadoSala;
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public final class SalasModulo extends ModuloSwing {
    private final SalasApi salasApi;
    private final CarteleraApi carteleraApi;
    private final PersonalApi personalApi;
    private final boolean empleado;
    private final DefaultTableModel salasModel = modelo("ID", "Nombre", "Capacidad", "Estado", "Detalle");
    private final DefaultTableModel funcionesModel = modelo("ID", "Fecha", "Horario", "Película", "Sala", "Formato", "Idioma", "Precio");
    private final JTable salasTabla = tabla(salasModel);
    private final JTable funcionesTabla = tabla(funcionesModel);
    private final JComboBox<SalaOpcion> salaButacas = new JComboBox<>();
    private final JPanel mapaButacas = new JPanel(new BorderLayout());
    private final JLabel resumenButacas = new JLabel("Seleccioná una sala.");
    private List<JsonNode> butacasActuales = new ArrayList<>();
    private Integer butacaSeleccionadaId;
    private final JPanel salasPanel;
    private final JPanel funcionesPanel;
    private final JPanel butacasPanel;

    public SalasModulo(ApiClient apiClient, boolean empleado, Component padre, Consumer<String> estado) {
        super(padre, estado);
        this.salasApi = new SalasApi(apiClient);
        this.carteleraApi = new CarteleraApi(apiClient);
        this.personalApi = new PersonalApi(apiClient);
        this.empleado = empleado;
        salasPanel = crearPanelSalas();
        funcionesPanel = crearPanelFunciones();
        butacasPanel = crearPanelButacas();
        instalarDetalleDobleClick(salasTabla, "Información de la sala", this::descripcionSalaSeleccionada,
                "Cambiar estado", this::mostrarSelectorEstadoSala);
        instalarDetalleDobleClick(funcionesTabla, "Información de la función",
                () -> descripcionFila(funcionesTabla), empleado ? null : "Modificar horario",
                empleado ? null : this::mostrarFormularioHorario);
    }

    public JPanel panelSalas() {
        return salasPanel;
    }
    public JPanel panelFunciones() {
        return funcionesPanel;
    }
    public JPanel panelButacas() {
        return butacasPanel;
    }

    @Override
    public void recargar() {
        cargarFunciones();
        cargarSalas();
        cargarButacas();
    }

    private boolean esEmpleado() {
        return empleado;
    }

    private JPanel crearPanelSalas() {
        return new SalasPanel(
                new PanelConTabla("Salas del cine", salasTabla),
                esEmpleado(),
                this::mostrarSelectorEstadoSala,
                this::cargarSalas
        );
    }

    private JPanel crearPanelFunciones() {
        return new FuncionesPanel(
                new PanelConTabla("Funciones programadas", funcionesTabla),
                this::cargarFunciones
        );
    }

    private JPanel crearPanelButacas() {
        return new ButacasPanel(
                salaButacas,
                mapaButacas,
                resumenButacas,
                esEmpleado(),
                this::actualizarMapaButacas,
                () -> {
                    cargarSalas();
                    cargarButacas();
                },
                () -> cambiarEstadoButaca("ocupar"),
                () -> cambiarEstadoButaca("liberar"),
                this::mostrarFormularioButacaFueraServicio,
                this::habilitarButacaFueraServicio
        );
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
                .append(campoHtml("Detalle", salasModel.getValueAt(fila, 4)))
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

    private void mostrarSelectorEstadoSala() {
        int filaVista = salasTabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarAviso("Seleccioná primero una sala.");
            return;
        }
        int fila = salasTabla.convertRowIndexToModel(filaVista);
        int salaId = ((Number) salasModel.getValueAt(fila, 0)).intValue();
        String estadoActual = String.valueOf(salasModel.getValueAt(fila, 3));
        ejecutar(
                "Cargando empleados...",
                personalApi::listarEmpleadosActivos,
                empleados -> mostrarFormularioEstadoSala(
                        salaId, estadoActual, empleados
                )
        );
    }

    private void mostrarFormularioEstadoSala(int salaId, String estadoActual,
                                             JsonNode empleados) {
        EstadoSalaDialog.mostrar(padre, estadoActual, empleados).ifPresent(datos -> {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("estado", datos.estado());
            cuerpo.put("descripcion", datos.descripcion());
            if (datos.destinatarioEmpleadoId() != null) {
                cuerpo.put("destinatarioEmpleadoId", datos.destinatarioEmpleadoId());
            }

            ejecutar("Actualizando sala...",
                    () -> salasApi.cambiarEstado(salaId, cuerpo),
                    respuesta -> cargarSalas());
        });
    }

    private void cargarSalas() {
        ejecutar("Cargando salas...", salasApi::listarSalas, respuesta -> {
            Integer salaAnterior = salaSeleccionadaId();
            limpiar(salasModel);
            salaButacas.removeAllItems();
            respuesta.forEach(sala -> salasModel.addRow(new Object[]{
                    sala.path("id").asInt(), sala.path("nombre").asText(),
                    sala.path("capacidad").asInt(),
                    nombreEstadoSala(sala.path("estado").asText("DISPONIBLE")),
                    sala.path("detalleEstado").isNull() || sala.path("detalleEstado").asText().isBlank()
                            ? "-"
                            : sala.path("detalleEstado").asText()
            }));
            respuesta.forEach(sala -> salaButacas.addItem(new SalaOpcion(
                    sala.path("id").asInt(), sala.path("nombre").asText()
            )));
            seleccionarSala(salaAnterior);
            actualizarMapaButacas();
        });
    }

    private void cargarFunciones() {
        ejecutar("Cargando funciones...", carteleraApi::listarFunciones, respuesta -> {
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
        ejecutar("Cargando butacas...", salasApi::listarButacas, respuesta -> {
            butacasActuales = new ArrayList<>();
            respuesta.forEach(butaca -> {
                butacasActuales.add(butaca);
            });
            actualizarMapaButacas();
        });
    }

    private void cambiarEstadoButaca(String operacion) {
        if (butacaSeleccionadaId == null) {
            mostrarAviso("Seleccioná una butaca del plano.");
            return;
        }
        if (esEmpleado()) {
            mostrarAviso("Los empleados solo pueden marcar butacas fuera de servicio.");
            return;
        }
        int id = butacaSeleccionadaId;
        ejecutar("Actualizando butaca...", () -> "ocupar".equals(operacion)
                ? salasApi.ocuparButaca(id) : salasApi.liberarButaca(id),
                respuesta -> cargarButacas());
    }

    private void mostrarFormularioButacaFueraServicio() {
        if (butacaSeleccionadaId == null) {
            mostrarAviso("Seleccioná una butaca del plano.");
            return;
        }
        JsonNode butaca = butacasActuales.stream()
                .filter(item -> item.path("id").asInt() == butacaSeleccionadaId)
                .findFirst()
                .orElse(null);
        if (butaca == null) {
            mostrarAviso("No se encontró la butaca seleccionada.");
            return;
        }
        ejecutar(
                "Cargando empleados...",
                personalApi::listarEmpleadosActivos,
                empleados -> mostrarFormularioAvisoButaca(butaca, empleados)
        );
    }

    private void habilitarButacaFueraServicio() {
        if (butacaSeleccionadaId == null) {
            mostrarAviso("Seleccioná una butaca del plano.");
            return;
        }
        JsonNode butaca = butacasActuales.stream()
                .filter(item -> item.path("id").asInt() == butacaSeleccionadaId)
                .findFirst()
                .orElse(null);
        if (butaca == null || !"FUERA_DE_SERVICIO".equals(butaca.path("estado").asText())) {
            mostrarAviso("Solo se puede volver a habilitar una butaca fuera de servicio.");
            return;
        }

        int id = butacaSeleccionadaId;
        ejecutar(
                "Habilitando butaca...",
                () -> salasApi.habilitarButaca(id),
                respuesta -> cargarButacas()
        );
    }

    private void mostrarFormularioAvisoButaca(JsonNode butaca, JsonNode empleados) {
        int id = butaca.path("id").asInt();
        EstadoButacaDialog.mostrar(padre, butaca.path("salaNombre").asText(),
                butaca.path("butacaNombre").asText(), empleados).ifPresent(datos -> {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("descripcion", datos.descripcion());
            if (datos.destinatarioEmpleadoId() != null) {
                cuerpo.put("destinatarioEmpleadoId", datos.destinatarioEmpleadoId());
            }

            ejecutar("Actualizando butaca y enviando aviso...",
                    () -> salasApi.marcarButacaFueraDeServicio(id, cuerpo),
                    respuesta -> cargarButacas());
        });
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

        if (butacasSala.stream().noneMatch(butaca -> butaca.path("id").asInt() == valorOCero(butacaSeleccionadaId))) {
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

    private int valorOCero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private Color colorEstadoButaca(String estadoButaca) {
        return switch (estadoButaca) {
            case "OCUPADA", "BLOQUEADA" -> new Color(105, 110, 118);
            case "FUERA_DE_SERVICIO" -> new Color(190, 52, 52);
            default -> new Color(46, 145, 82);
        };
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
                padre, formulario, "Modificar horario",
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
                () -> carteleraApi.actualizarHorario(funcionId, cuerpo),
                respuesta -> cargarFunciones());
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
}
