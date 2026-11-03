package desktop.comun.paneles;

import static desktop.comun.util.BotonesSwing.boton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

public class ButacasPanel extends JPanel {

    public ButacasPanel(JComboBox<?> selectorSala, JPanel mapaButacas, JLabel resumen,
                        boolean empleado, Runnable actualizarMapa, Runnable recargar,
                        Runnable ocupar, Runnable liberar, Runnable fueraServicio,
                        Runnable volverHabilitar) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel superior = new JPanel(new BorderLayout(12, 0));
        JLabel titulo = new JLabel("Plano de butacas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton actualizar = new JButton("Recargar");
        actualizar.addActionListener(event -> recargar.run());
        selectorSala.addActionListener(event -> actualizarMapa.run());
        selector.add(new JLabel("Sala:"));
        selector.add(selectorSala);
        selector.add(actualizar);
        superior.add(titulo, BorderLayout.WEST);
        superior.add(selector, BorderLayout.EAST);

        JScrollPane scrollMapa = new JScrollPane(mapaButacas);
        scrollMapa.getVerticalScrollBar().setUnitIncrement(18);
        scrollMapa.getHorizontalScrollBar().setUnitIncrement(18);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(resumen);
        acciones.add(Box.createHorizontalStrut(18));
        if (!empleado) {
            acciones.add(boton("Ocupar", ocupar));
            acciones.add(boton("Liberar", liberar));
        }
        acciones.add(boton("Fuera de servicio", fueraServicio));
        if (empleado) {
            acciones.add(boton("Volver a habilitar", volverHabilitar));
        }

        JLabel ayuda = new JLabel(empleado
                ? "Seleccioná una butaca para marcarla fuera de servicio o volver a habilitarla."
                : "Seleccioná una butaca en el plano y usá los botones inferiores para cambiar su estado.");
        ayuda.setForeground(new java.awt.Color(92, 98, 108));
        ayuda.setFont(ayuda.getFont().deriveFont(Font.ITALIC, 12f));
        ayuda.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 8));

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(superior, BorderLayout.NORTH);
        encabezado.add(ayuda, BorderLayout.SOUTH);

        add(encabezado, BorderLayout.NORTH);
        add(scrollMapa, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

}
