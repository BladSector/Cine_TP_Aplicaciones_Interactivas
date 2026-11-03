package desktop.duenio.paneles;

import javax.swing.JTable;

public class ProductosPanel extends CrudAdministracionPanel {
    public ProductosPanel(JTable tabla, Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super("Productos de confitería", "Nuevo", tabla, nuevo, recargar, verDetalle);
    }
}
