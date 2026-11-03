package desktop.duenio.modulos;

import desktop.comun.ApiClient;
import desktop.comun.modulos.ModuloSwing;
import desktop.comun.api.PersonalApi;
import desktop.comun.componentes.PanelConTabla;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.tabla;
import desktop.duenio.paneles.EmpleadosPanel;
import desktop.duenio.dialogos.EmpleadoDialog;
import desktop.duenio.dialogos.RestablecerContraseniaDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.util.Map;
import java.util.function.Consumer;

public final class PersonalModulo extends ModuloSwing {
    private final PersonalApi personalApi;
    private final DefaultTableModel empleadosModel = modelo("ID", "Nombre", "Apellido", "Usuario", "Activo");
    private final JTable empleadosTabla = tabla(empleadosModel);
    private final JPanel panel;

    public PersonalModulo(ApiClient apiClient, Component padre, Consumer<String> estado) {
        super(padre, estado);
        this.personalApi = new PersonalApi(apiClient);
        panel = crearPanelEmpleados();
        instalarDetalleDobleClick(empleadosTabla, "Información del empleado", () -> descripcionFila(empleadosTabla));
    }

    public JPanel panel() {
        return panel;
    }

    @Override
    public void recargar() {
        cargarEmpleados();
    }

    private JPanel crearPanelEmpleados() {
        return new EmpleadosPanel(
                new PanelConTabla("Personal del cine", empleadosTabla),
                this::mostrarFormularioEmpleado,
                this::mostrarRestablecimientoContrasenia,
                () -> cambiarEstadoEmpleado("activar"),
                () -> cambiarEstadoEmpleado("desactivar"),
                this::cargarEmpleados
        );
    }

    private void cargarEmpleados() {
        ejecutar("Cargando empleados...", personalApi::listarEmpleados, respuesta -> {
            limpiar(empleadosModel);
            respuesta.forEach(empleado -> empleadosModel.addRow(new Object[]{
                    empleado.path("id").asInt(), empleado.path("nombre").asText(),
                    empleado.path("apellido").asText(), empleado.path("usuario").asText(),
                    empleado.path("activo").asBoolean()
            }));
        });
    }

    private void cambiarEstadoEmpleado(String operacion) {
        Integer id = idSeleccionado(empleadosTabla);
        if (id == null) {
            return;
        }
        ejecutar("Actualizando empleado...",
                () -> "activar".equals(operacion)
                        ? personalApi.activarEmpleado(id) : personalApi.desactivarEmpleado(id),
                respuesta -> cargarEmpleados());
    }

    private void mostrarRestablecimientoContrasenia() {
        Integer empleadoId = idSeleccionado(empleadosTabla);
        if (empleadoId == null) {
            return;
        }

        int filaVista = empleadosTabla.getSelectedRow();
        int fila = empleadosTabla.convertRowIndexToModel(filaVista);
        String usuario = String.valueOf(empleadosModel.getValueAt(fila, 3));
        RestablecerContraseniaDialog.mostrar(padre, usuario).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "nuevaContrasenia", datos.nuevaContrasenia(),
                    "contraseniaConfirmacion", datos.contraseniaConfirmacion());
            ejecutar("Restableciendo contraseña...",
                    () -> personalApi.restablecerContrasenia(empleadoId, cuerpo),
                    respuesta -> JOptionPane.showMessageDialog(padre,
                            "La contraseña de " + usuario + " fue actualizada.",
                            "Contraseña restablecida", JOptionPane.INFORMATION_MESSAGE));
        });
    }

    private void mostrarFormularioEmpleado() {
        EmpleadoDialog.mostrar(padre).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of(
                    "nombre", datos.nombre(),
                    "apellido", datos.apellido(),
                    "usuario", datos.usuario(),
                    "contrasenia", datos.contrasenia());
            ejecutar("Creando empleado...", () -> personalApi.crearEmpleado(cuerpo),
                    respuesta -> cargarEmpleados());
        });
    }
}
