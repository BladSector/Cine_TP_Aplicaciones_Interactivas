package desktop.comun.util;

import javax.swing.JButton;

public final class BotonesSwing {
    private BotonesSwing() {}

    public static JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(event -> accion.run());
        return boton;
    }
}
