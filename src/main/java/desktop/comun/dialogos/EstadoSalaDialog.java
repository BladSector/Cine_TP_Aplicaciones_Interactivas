package desktop.comun.dialogos;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.fasterxml.jackson.databind.JsonNode;

import static desktop.comun.util.FormatoSwing.codigoEstadoSala;

public final class EstadoSalaDialog {
    private EstadoSalaDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, String estadoActual, JsonNode empleados) {
        JComboBox<String> estados = new JComboBox<>(new String[]{
                "Disponible", "Limpieza", "Cerrada", "Problema particular"
        });
        estados.setSelectedItem(estadoActual);

        JTextArea descripcion = new JTextArea(4, 24);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        SelectorDestinatario destinatario = new SelectorDestinatario(empleados);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Estado"));
        formulario.add(estados);
        formulario.add(new JLabel("Descripción"));
        formulario.add(new JScrollPane(descripcion));
        formulario.add(new JLabel("Avisar a"));
        formulario.add(destinatario);

        int opcion = JOptionPane.showConfirmDialog(padre, formulario, "Nuevo estado de la sala",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }

        String estado = codigoEstadoSala(String.valueOf(estados.getSelectedItem()));
        String detalle = descripcion.getText().trim();
        if ("PROBLEMA_PARTICULAR".equals(estado) && detalle.isBlank()) {
            JOptionPane.showMessageDialog(padre, "Describí el problema particular de la sala.",
                    "Atención", JOptionPane.WARNING_MESSAGE);
            return Optional.empty();
        }
        return Optional.of(new Datos(estado, detalle, destinatario.getEmpleadoId()));
    }

    public record Datos(String estado, String descripcion, Integer destinatarioEmpleadoId) {
    }
}
