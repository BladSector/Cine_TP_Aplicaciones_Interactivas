package desktop.comun.componentes;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

public class PanelConTabla extends JPanel {
    public PanelConTabla(String titulo, JTable tabla) {
        this(titulo, tabla, 12,
                "Seleccioná una fila para operar. Hacé doble clic para ver toda la información.");
    }

    public PanelConTabla(String titulo, JTable tabla, int margen, String ayuda) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(margen, margen, margen, margen));
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 15f));
        cabecera.add(etiqueta, BorderLayout.WEST);
        cabecera.add(new BuscadorTabla(tabla), BorderLayout.EAST);
        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(cabecera, BorderLayout.NORTH);
        encabezado.add(etiquetaAyuda(ayuda), BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public static JLabel etiquetaAyuda(String texto) {
        JLabel ayuda = new JLabel(texto);
        ayuda.setForeground(new Color(92, 98, 108));
        ayuda.setFont(ayuda.getFont().deriveFont(Font.ITALIC, 12f));
        ayuda.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 8));
        return ayuda;
    }
}
