package desktop.empleado.paneles;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class SalasPanel extends JPanel {

    public SalasPanel(JPanel tablaPanel, boolean permitirCambioEstado,
                      Runnable cambiarEstado, Runnable recargar) {
        super(new BorderLayout());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (permitirCambioEstado) {
            JButton cambiar = new JButton("Cambiar estado");
            cambiar.addActionListener(event -> cambiarEstado.run());
            acciones.add(cambiar);
        }

        JButton actualizar = new JButton("Recargar");
        actualizar.addActionListener(event -> recargar.run());
        acciones.add(actualizar);

        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }
}
