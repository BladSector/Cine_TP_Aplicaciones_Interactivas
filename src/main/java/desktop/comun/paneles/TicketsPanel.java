package desktop.comun.paneles;

import static desktop.comun.util.BotonesSwing.boton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class TicketsPanel extends JPanel {

    public TicketsPanel(boolean empleado, JTable tabla, JPanel tablaPanel, JTextField codigoQr,
                        Consumer<Integer> seleccionarTicket, Runnable validarQr,
                        Runnable validarManual, Runnable reembolsar, Runnable recargar) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        JLabel titulo = new JLabel(empleado ? "Control de acceso y tickets" : "Ventas y tickets");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 17f));
        JPanel encabezado = new JPanel(new GridLayout(0, 1, 0, 4));
        encabezado.add(titulo);
        encabezado.add(new JLabel("Escaneá el QR o pegá el código del ticket y presioná Enter."));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton buscar = new JButton("Validar por QR");
        buscar.addActionListener(event -> validarQr.run());
        codigoQr.addActionListener(event -> validarQr.run());
        busqueda.add(new JLabel("Código QR:"));
        busqueda.add(codigoQr);
        busqueda.add(buscar);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(busqueda, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            if (tabla.getSelectedRow() < 0) {
                seleccionarTicket.accept(null);
                return;
            }
            int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
            seleccionarTicket.accept(((Number) tabla.getModel().getValueAt(fila, 0)).intValue());
        });

        tablaPanel.setBorder(BorderFactory.createEmptyBorder());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Validar entradas manualmente", validarManual));
        acciones.add(boton("Reembolsar ticket", reembolsar));
        acciones.add(boton("Recargar", recargar));

        add(superior, BorderLayout.NORTH);
        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

}
