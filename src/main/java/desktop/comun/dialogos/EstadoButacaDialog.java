package desktop.comun.dialogos;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;

public final class EstadoButacaDialog {
    private EstadoButacaDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, String sala, String butaca, JsonNode empleados) {
        JTextArea comentario = new JTextArea(4, 24);
        comentario.setLineWrap(true);
        comentario.setWrapStyleWord(true);
        SelectorDestinatario destinatario = new SelectorDestinatario(empleados);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Butaca"));
        formulario.add(new JLabel(sala + " - " + butaca));
        formulario.add(new JLabel("Comentario (opcional)"));
        formulario.add(new JScrollPane(comentario));
        formulario.add(new JLabel("Avisar a"));
        formulario.add(destinatario);

        int opcion = JOptionPane.showConfirmDialog(padre, formulario, "Marcar butaca fuera de servicio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return Optional.of(new Datos(comentario.getText().trim(), destinatario.getEmpleadoId()));
    }

    public record Datos(String descripcion, Integer destinatarioEmpleadoId) {
    }
}
