package desktop.duenio.dialogos;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;

public final class RestablecerContraseniaDialog {
    private RestablecerContraseniaDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, String usuario) {
        JPasswordField nuevaContrasenia = new JPasswordField();
        JPasswordField repetirContrasenia = new JPasswordField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Empleado"));
        formulario.add(new JLabel(usuario));
        formulario.add(new JLabel("Nueva contraseña"));
        formulario.add(nuevaContrasenia);
        formulario.add(new JLabel("Repetir contraseña"));
        formulario.add(repetirContrasenia);

        int opcion = JOptionPane.showConfirmDialog(padre, formulario, "Restablecer contraseña",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }

        String clave = new String(nuevaContrasenia.getPassword());
        String confirmacion = new String(repetirContrasenia.getPassword());
        if (!FormularioDialogo.contraseniasValidas(padre, clave, confirmacion)) {
            return Optional.empty();
        }
        return Optional.of(new Datos(clave, confirmacion));
    }

    public record Datos(String nuevaContrasenia, String contraseniaConfirmacion) {
    }
}
