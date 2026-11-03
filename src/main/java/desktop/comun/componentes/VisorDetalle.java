package desktop.comun.componentes;

import javax.swing.JEditorPane;
import java.awt.Font;
import static desktop.comun.util.DetalleHtml.mensajeHtml;

public class VisorDetalle extends JEditorPane {
    public VisorDetalle(String mensajeInicial) {
        super("text/html", mensajeHtml(mensajeInicial));
        setEditable(false);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }
}
