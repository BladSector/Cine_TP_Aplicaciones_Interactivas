package desktop.duenio.paneles;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

public class ActividadPanel extends JPanel {
    public ActividadPanel(JPanel tablaPanel, Runnable recargar) {
        super(new BorderLayout());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton actualizar = new JButton("Recargar actividad");
        actualizar.addActionListener(event -> recargar.run());
        acciones.add(actualizar);
        acciones.add(Box.createHorizontalStrut(18));
        acciones.add(etiquetaPerfil("Dueño", new Color(255, 244, 196)));
        acciones.add(etiquetaPerfil("Empleado", new Color(218, 234, 252)));

        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

    private JLabel etiquetaPerfil(String texto, Color color) {
        JLabel etiqueta = new JLabel("  " + texto + "  ");
        etiqueta.setOpaque(true);
        etiqueta.setBackground(color);
        etiqueta.setBorder(BorderFactory.createLineBorder(color.darker()));
        return etiqueta;
    }
}
