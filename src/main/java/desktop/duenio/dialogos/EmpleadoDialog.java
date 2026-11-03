package desktop.duenio.dialogos;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;

public final class EmpleadoDialog {
    private EmpleadoDialog() {
    }

    public static Optional<Datos> mostrar(Component padre) {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField usuario = new JTextField();
        JPasswordField contrasenia = new JPasswordField();
        JPasswordField repetirContrasenia = new JPasswordField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Nombre"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido"));
        formulario.add(apellido);
        formulario.add(new JLabel("Usuario"));
        formulario.add(usuario);
        formulario.add(new JLabel("Contraseña"));
        formulario.add(contrasenia);
        formulario.add(new JLabel("Repetir contraseña"));
        formulario.add(repetirContrasenia);

        int opcion = JOptionPane.showConfirmDialog(padre, formulario, "Nuevo empleado",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }

        String clave = new String(contrasenia.getPassword());
        String confirmacion = new String(repetirContrasenia.getPassword());
        if (!FormularioDialogo.contraseniasValidas(padre, clave, confirmacion)) {
            return Optional.empty();
        }
        return Optional.of(new Datos(nombre.getText().trim(), apellido.getText().trim(),
                usuario.getText().trim(), clave));
    }

    public record Datos(String nombre, String apellido, String usuario, String contrasenia) {
    }
}
