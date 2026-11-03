package desktop.duenio.paneles;

import javax.swing.JTable;

public class SalasAdminPanel extends CrudAdministracionPanel {
    public SalasAdminPanel(JTable tabla, Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super("Salas", "Nueva sala", tabla, nuevo, recargar, verDetalle);
    }
}
