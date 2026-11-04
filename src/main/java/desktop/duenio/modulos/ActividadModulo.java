package desktop.duenio.modulos;

import desktop.comun.ApiClient;
import desktop.comun.modulos.ModuloSwing;
import desktop.comun.api.AuditoriaApi;
import desktop.comun.componentes.PanelConTabla;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.tabla;
import desktop.duenio.paneles.ActividadPanel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.function.Consumer;

public final class ActividadModulo extends ModuloSwing {
    private final AuditoriaApi auditoriaApi;
    private final DefaultTableModel auditoriasModel = modelo("ID", "Fecha y hora", "Usuario", "Perfil", "Acción",
            "Entidad", "Entidad ID", "Resultado", "Detalle");
    private final JTable auditoriasTabla = tabla(auditoriasModel);
    private final JPanel panel;

    public ActividadModulo(ApiClient apiClient, Component padre, Consumer<String> estado) {
        super(padre, estado);
        this.auditoriaApi = new AuditoriaApi(apiClient);
        configurarColoresActividad();
        panel = crearPanelAuditoria();
        instalarDetalleDobleClick(auditoriasTabla, "Detalle de la actividad", () -> descripcionFila(auditoriasTabla));
    }

    public JPanel panel() {
        return panel;
    }

    @Override
    public void recargar() {
        cargarAuditorias();
    }

    private JPanel crearPanelAuditoria() {
        return new ActividadPanel(
                new PanelConTabla("Actividad del sistema", auditoriasTabla),
                this::cargarAuditorias
        );
    }

    private void cargarAuditorias() {
        consultar("actividad", "Cargando actividad...", auditoriaApi::listarActividad, respuesta -> {
            limpiar(auditoriasModel);
            respuesta.forEach(auditoria -> {
                String usuario = auditoria.path("usuario").asText();
                String perfil = nombrePerfilAuditoria(auditoria.path("tipoActor").asText());
                String entidad = nombreEntidadAuditoria(auditoria.path("entidad").asText());
                String entidadId = textoNullable(auditoria.get("entidadId"));
                boolean exitosa = "EXITOSA".equals(auditoria.path("resultado").asText());
                String accion = nombreAccionAuditoria(
                        auditoria.path("accion").asText(),
                        auditoria.path("entidad").asText()
                );
                String codigoAccion = auditoria.path("accion").asText();
                String detalleRegistrado = auditoria.path("detalle").asText();

                auditoriasModel.addRow(new Object[]{
                        auditoria.path("id").asLong(),
                        auditoria.path("fechaHora").asText().replace('T', ' '),
                        usuario,
                        perfil,
                        accion,
                        entidad,
                        entidadId,
                        exitosa ? "Completada" : "Fallida",
                        detalleActividad(
                                usuario, perfil, accion, entidadId, exitosa,
                                codigoAccion, detalleRegistrado
                        )
                });
            });
        });
    }

    private void configurarColoresActividad() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                            boolean isSelected, boolean hasFocus,
                                                            int row, int column) {
                Component componente = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );
                if (!isSelected) {
                    int filaModelo = table.convertRowIndexToModel(row);
                    int columnaPerfil = auditoriasModel.findColumn("Perfil");
                    String perfil = String.valueOf(auditoriasModel.getValueAt(filaModelo, columnaPerfil));
                    componente.setBackground("Dueño".equals(perfil)
                            ? new Color(255, 248, 218)
                            : new Color(232, 242, 254));
                    componente.setForeground(table.getForeground());
                }
                setHorizontalAlignment(column == 0 ? JLabel.RIGHT : JLabel.LEFT);
                return componente;
            }
        };
        for (int columna = 0; columna < auditoriasTabla.getColumnCount(); columna++) {
            auditoriasTabla.getColumnModel().getColumn(columna).setCellRenderer(renderer);
        }
    }

    private static String nombrePerfilAuditoria(String tipoActor) {
        return "DUEÑO".equals(tipoActor) || "DUENIO".equals(tipoActor) ? "Dueño" : "Empleado";
    }

    private static String nombreEntidadAuditoria(String entidad) {
        return switch (entidad) {
            case "CATEGORIA" -> "Categoría";
            case "PELICULA" -> "Película";
            case "SALA" -> "Sala";
            case "FUNCION" -> "Función";
            case "BUTACA" -> "Butaca";
            case "PRODUCTO_CONFITERIA" -> "Producto de confitería";
            case "ENTRADA" -> "Entrada";
            case "TICKET" -> "Ticket";
            case "CONSUMO" -> "Consumo";
            case "EMPLEADO" -> "Empleado";
            case "SESION" -> "Sesión";
            case "COMPRA" -> "Compra";
            default -> "Sistema";
        };
    }

    private static String nombreAccionAuditoria(String accion, String entidad) {
        return switch (accion) {
            case "INICIAR_SESION" -> "Inició sesión";
            case "CERRAR_SESION" -> "Cerró sesión";
            case "RESTABLECER_CONTRASENA" -> "Restableció una contraseña";
            case "ACTIVAR_EMPLEADO" -> "Activó un empleado";
            case "DESACTIVAR_EMPLEADO" -> "Desactivó un empleado";
            case "MODIFICAR_HORARIO" -> "Modificó el horario de una función";
            case "CAMBIAR_ESTADO_SALA" -> "Cambió el estado de una sala";
            case "MODIFICAR_DISTRIBUCION_SALA" -> "Modificó la distribución de una sala";
            case "OCUPAR_BUTACA" -> "Marcó una butaca como ocupada";
            case "LIBERAR_BUTACA" -> "Liberó una butaca";
            case "MARCAR_BUTACA_FUERA_DE_SERVICIO" -> "Marcó una butaca fuera de servicio";
            case "HABILITAR_BUTACA" -> "Volvió a habilitar una butaca";
            case "VALIDAR_ENTRADAS" -> "Validó las entradas de un ticket";
            case "ENTREGAR_CONSUMOS" -> "Entregó los consumos de un ticket";
            case "PROCESAR_INGRESO" -> "Procesó el ingreso de un ticket";
            case "REEMBOLSAR_ENTRADA" -> "Reembolsó una entrada";
            case "REEMBOLSAR_TICKET" -> "Reembolsó un ticket";
            case "CREAR" -> "Creó " + entidadConArticulo(entidad);
            case "ACTUALIZAR" -> "Actualizó " + entidadConArticulo(entidad);
            case "ELIMINAR" -> "Eliminó " + entidadConArticulo(entidad);
            default -> "Realizó una operación";
        };
    }

    private static String entidadConArticulo(String entidad) {
        return switch (entidad) {
            case "CATEGORIA" -> "una categoría";
            case "PELICULA" -> "una película";
            case "SALA" -> "una sala";
            case "FUNCION" -> "una función";
            case "BUTACA" -> "una butaca";
            case "PRODUCTO_CONFITERIA" -> "un producto de confitería";
            case "ENTRADA" -> "una entrada";
            case "TICKET" -> "un ticket";
            case "CONSUMO" -> "un consumo";
            case "EMPLEADO" -> "un empleado";
            case "COMPRA" -> "una compra";
            default -> "un registro";
        };
    }

    private static String detalleActividad(String usuario, String perfil, String accion,
                                           String entidadId, boolean exitosa,
                                           String codigoAccion, String detalleRegistrado) {
        String accionEnFrase = accion.isEmpty()
                ? "realizó una operación"
                : Character.toLowerCase(accion.charAt(0)) + accion.substring(1);
        String referencia = "-".equals(entidadId) ? "" : " (registro #" + entidadId + ")";
        String resultado = exitosa ? " correctamente." : ", pero no pudo completarse.";
        String informacionAdicional = "CAMBIAR_ESTADO_SALA".equals(codigoAccion)
                && detalleRegistrado != null && !detalleRegistrado.isBlank()
                ? " " + detalleRegistrado
                : "";
        return usuario + " (" + perfil + ") " + accionEnFrase + referencia
                + resultado + informacionAdicional;
    }
}
