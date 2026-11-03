package desktop.duenio.dialogos;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class SalaDialog {
    private SalaDialog() {
    }

    public static Optional<Datos> mostrarNueva(Component padre) {
        return mostrar(padre, "Nueva sala", EditorDistribucionSala.nuevo());
    }

    public static Optional<Datos> mostrarEdicion(Component padre, String nombre, List<JsonNode> butacas) {
        return mostrar(padre, "Editar " + nombre, EditorDistribucionSala.desde(nombre, butacas));
    }

    private static Optional<Datos> mostrar(Component padre, String titulo, EditorDistribucionSala editor) {
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setPreferredSize(new Dimension(860, 560));
        int opcion = JOptionPane.showConfirmDialog(
                padre, scroll, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        if (editor.getNombre().isBlank()) {
            FormularioDialogo.aviso(padre, "El nombre de la Sala es obligatorio.");
            return Optional.empty();
        }
        if (editor.getUbicaciones().isEmpty()) {
            FormularioDialogo.aviso(padre, "La Sala debe tener al menos una Butaca.");
            return Optional.empty();
        }
        return Optional.of(new Datos(editor.getNombre(), editor.getUbicaciones()));
    }

    public record Datos(String nombre, List<Map<String, Object>> butacas) {
    }
}
