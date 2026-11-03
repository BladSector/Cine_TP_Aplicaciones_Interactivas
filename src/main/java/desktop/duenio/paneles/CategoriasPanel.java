package desktop.duenio.paneles;

import javax.swing.JTable;

public class CategoriasPanel extends CrudAdministracionPanel {
    public CategoriasPanel(JTable tabla, Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super("Categorías", "Nueva", tabla, nuevo, recargar, verDetalle);
    }
}
