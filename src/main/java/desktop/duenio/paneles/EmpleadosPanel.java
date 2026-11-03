package desktop.duenio.paneles;

import static desktop.comun.util.BotonesSwing.boton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class EmpleadosPanel extends JPanel {
    public EmpleadosPanel(JPanel tablaPanel, Runnable nuevo, Runnable restablecer,
                          Runnable activar, Runnable desactivar, Runnable recargar) {
        super(new BorderLayout());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Nuevo empleado", nuevo));
        acciones.add(boton("Restablecer contraseña", restablecer));
        acciones.add(boton("Activar", activar));
        acciones.add(boton("Desactivar", desactivar));
        acciones.add(boton("Recargar", recargar));

        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

}
