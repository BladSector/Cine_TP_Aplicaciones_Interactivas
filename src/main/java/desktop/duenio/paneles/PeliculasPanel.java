package desktop.duenio.paneles;

import javax.swing.JTable;

public class PeliculasPanel extends CrudAdministracionPanel {
    public PeliculasPanel(JTable tabla, Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super("Películas", "Nueva", tabla, nuevo, recargar, verDetalle);
    }
}
