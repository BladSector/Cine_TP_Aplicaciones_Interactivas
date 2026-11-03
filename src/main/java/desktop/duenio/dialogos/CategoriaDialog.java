package desktop.duenio.dialogos;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.util.Optional;

public final class CategoriaDialog {
    private CategoriaDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, String nombreActual) {
        JTextField nombre = new JTextField(nombreActual == null ? "" : nombreActual, 24);
        JPanel panel = FormularioDialogo.crear(new String[]{"Nombre"}, nombre);
        if (!FormularioDialogo.confirmar(padre, "Categoría", panel)) {
            return Optional.empty();
        }
        return Optional.of(new Datos(nombre.getText().trim()));
    }

    public record Datos(String nombre) {
    }
}
