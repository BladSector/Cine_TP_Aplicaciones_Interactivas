package desktop.comun.dialogos;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.JComboBox;

final class SelectorDestinatario extends JComboBox<SelectorDestinatario.Opcion> {
    SelectorDestinatario(JsonNode empleados) {
        addItem(new Opcion(0, "Todos los empleados"));
        empleados.forEach(empleado -> addItem(new Opcion(
                empleado.path("id").asInt(),
                empleado.path("nombre").asText() + " (" + empleado.path("usuario").asText() + ")"
        )));
    }

    Integer getEmpleadoId() {
        Opcion opcion = (Opcion) getSelectedItem();
        return opcion == null || opcion.id() == 0 ? null : opcion.id();
    }

    record Opcion(int id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }
}
