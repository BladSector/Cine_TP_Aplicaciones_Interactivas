package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.componentes.VisorDetalle;
import desktop.comun.modulos.ModuloSwing;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.tablaAdministracion;

public abstract class CrudModulo extends ModuloSwing {
    protected final DefaultTableModel modeloTabla;
    protected final JTable tabla;
    protected DatosAdministracion datos = DatosAdministracion.vacios();
    private final Runnable recargarTodo;
    private final String entidad;

    protected CrudModulo(Component padre, Consumer<String> estado, Runnable recargarTodo,
                         String entidad, String... columnas) {
        super(padre, estado);
        this.recargarTodo = recargarTodo;
        this.entidad = entidad;
        modeloTabla = modelo(columnas);
        tabla = tablaAdministracion(modeloTabla);
    }

    public abstract JPanel panel();
    protected abstract JsonNode registros();
    protected abstract void agregarFila(JsonNode registro);
    protected abstract void formulario(JsonNode registro);
    protected abstract String descripcion(JsonNode registro);
    protected abstract void eliminarRegistro(int id) throws Exception;

    @Override
    public final void recargar() {
        if (estaActivo()) recargarTodo.run();
    }

    public final void aplicarDatos(DatosAdministracion nuevosDatos) {
        if (!estaActivo()) return;
        datos = nuevosDatos;
        limpiar(modeloTabla);
        registros().forEach(this::agregarFila);
    }

    protected final void nuevo() {
        formulario(null);
    }

    protected List<AccionDetalle> acciones(JsonNode registro) {
        return List.of(new AccionDetalle("Editar", () -> formulario(registro)),
                new AccionDetalle("Eliminar", () -> confirmarEliminacion(registro)));
    }

    protected final void mostrarDetalle(String titulo) {
        Integer id = idSeleccionado(tabla);
        if (id == null) return;
        JsonNode registro = null;
        for (JsonNode item : registros()) {
            if (item.path("id").asInt() == id) {
                registro = item;
                break;
            }
        }
        if (registro == null) {
            mostrarAviso("No se encontró el registro seleccionado. Recargá los datos.");
            return;
        }

        // Las acciones conservan el registro mostrado aunque la tabla se recargue durante el diálogo.
        List<AccionDetalle> acciones = acciones(registro);
        Object[] opciones = new Object[acciones.size() + 1];
        opciones[0] = "Cerrar";
        for (int i = 0; i < acciones.size(); i++) opciones[i + 1] = acciones.get(i).nombre();
        JEditorPane detalle = new VisorDetalle("");
        detalle.setText(descripcion(registro));
        detalle.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(detalle);
        scroll.setPreferredSize(new Dimension(480, 320));
        int opcion = JOptionPane.showOptionDialog(padre, scroll,
                "Información de " + titulo.toLowerCase(), JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, opciones, opciones[0]);
        if (opcion > 0 && opcion <= acciones.size() && estaActivo()) {
            acciones.get(opcion - 1).ejecutar().run();
        }
    }

    protected final void guardar(boolean edicion, String nombre, Operacion<JsonNode> operacion) {
        ejecutar((edicion ? "Actualizando " : "Creando ") + nombre + "...",
                operacion, respuesta -> recargar());
    }

    protected final void confirmarEliminacion(JsonNode registro) {
        int id = registro.path("id").asInt();
        int respuesta = JOptionPane.showConfirmDialog(padre,
                "¿Seguro que querés eliminar " + entidad + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            ejecutar("Eliminando...", () -> {
                eliminarRegistro(id);
                return null;
            }, ignorado -> recargar());
        }
    }

    protected record AccionDetalle(String nombre, Runnable ejecutar) { }
}
