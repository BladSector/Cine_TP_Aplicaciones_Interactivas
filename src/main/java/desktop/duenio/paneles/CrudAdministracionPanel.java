package desktop.duenio.paneles;

import desktop.comun.componentes.PanelConTabla;
import static desktop.comun.util.BotonesSwing.boton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class CrudAdministracionPanel extends JPanel {

    protected CrudAdministracionPanel(String titulo, String textoNuevo, JTable tabla,
                                      Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() != 2
                        || event.getButton() != java.awt.event.MouseEvent.BUTTON1) {
                    return;
                }
                int fila = tabla.rowAtPoint(event.getPoint());
                if (fila >= 0) {
                    tabla.setRowSelectionInterval(fila, fila);
                    verDetalle.run();
                }
            }
        });

        JPanel tablaPanel = new PanelConTabla(titulo, tabla, 0,
                "Seleccioná una fila para operar. Hacé doble clic para ver la información y las acciones disponibles.");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton(textoNuevo, nuevo));
        acciones.add(boton("Recargar", recargar));

        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

}
