package desktop.comun.modulos;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.databind.JsonNode;

import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.comun.api.PersonalApi;
import desktop.comun.api.SalasApi;
import desktop.comun.componentes.PanelConTabla;
import desktop.comun.componentes.MapaButacasPanel;
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
    private final MapaButacasPanel mapaButacas = new MapaButacasPanel(
            id -> {
                butacaSeleccionadaId = id;
                actualizarMapaButacas();
            },
            butaca -> mostrarDetalle("Información de la butaca", descripcionButaca(butaca))
    );
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
        consultar("salas", "Cargando salas...", salasApi::listarSalas, respuesta -> {
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
        consultar("funciones", "Cargando funciones...", carteleraApi::listarFunciones, respuesta -> {
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
        consultar("butacas", "Cargando butacas...", salasApi::listarButacas, respuesta -> {
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
        SalaOpcion sala = (SalaOpcion) salaButacas.getSelectedItem();
        if (sala == null) {
            butacaSeleccionadaId = null;
            mapaButacas.mostrarSinSala();
            actualizarResumenButaca();
            return;
        }
        List<JsonNode> butacasSala = butacasActuales.stream()
                .filter(butaca -> butaca.path("salaId").asInt() == sala.id())
                .sorted(Comparator.comparing((JsonNode butaca) -> butaca.path("fila").asText())
                        .thenComparingInt(butaca -> butaca.path("numero").asInt()))
                .toList();
        if (butacasSala.stream().noneMatch(butaca -> butaca.path("id").asInt() == valorOCero(butacaSeleccionadaId))) {
            butacaSeleccionadaId = null;
        }
        mapaButacas.mostrar(butacasSala, butacaSeleccionadaId);
        actualizarResumenButaca();
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

}
