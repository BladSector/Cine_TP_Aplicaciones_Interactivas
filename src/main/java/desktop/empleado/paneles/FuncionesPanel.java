package desktop.empleado.paneles;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class FuncionesPanel extends JPanel {

    public FuncionesPanel(JPanel tablaPanel, Runnable recargar) {
        super(new BorderLayout());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton actualizar = new JButton("Recargar funciones");
        actualizar.addActionListener(event -> recargar.run());
        acciones.add(actualizar);

        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }
}
