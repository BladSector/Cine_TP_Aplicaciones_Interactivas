package desktop.duenio.paneles;

import javax.swing.JTable;

public class FuncionesAdminPanel extends CrudAdministracionPanel {
    public FuncionesAdminPanel(JTable tabla, Runnable nuevo, Runnable recargar, Runnable verDetalle) {
        super("Funciones", "Nueva función", tabla, nuevo, recargar, verDetalle);
    }
}
