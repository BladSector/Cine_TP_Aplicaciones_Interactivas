package desktop.comun.paneles;

import static desktop.comun.util.BotonesSwing.boton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class ConsumosPanel extends JPanel {

    public ConsumosPanel(JPanel tablaPanel, JTextField codigoQr, Runnable validarQr,
                         Runnable entregarManual, Runnable recargar) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        JLabel titulo = new JLabel("Consumos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 17f));
        JPanel encabezado = new JPanel(new GridLayout(0, 1, 0, 4));
        encabezado.add(titulo);
        encabezado.add(new JLabel(
                "Escaneá el QR o pegá el código del ticket para entregar sus consumos."
        ));

        JPanel lectorQr = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton entregarQr = new JButton("Validar con QR");
        entregarQr.addActionListener(event -> validarQr.run());
        codigoQr.addActionListener(event -> validarQr.run());
        lectorQr.add(new JLabel("Código QR:"));
        lectorQr.add(codigoQr);
        lectorQr.add(entregarQr);
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(lectorQr, BorderLayout.SOUTH);

        tablaPanel.setBorder(BorderFactory.createEmptyBorder());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Entregar seleccionado manualmente", entregarManual));
        acciones.add(boton("Recargar", recargar));

        add(superior, BorderLayout.NORTH);
        add(tablaPanel, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);
    }

}
