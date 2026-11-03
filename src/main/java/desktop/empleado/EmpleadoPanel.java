package desktop.empleado;

import desktop.comun.ApiClient;
import desktop.comun.PanelPersonalBase;
import desktop.comun.modulos.SalasModulo;
import desktop.comun.modulos.VentasModulo;
import desktop.empleado.modulos.AvisosModulo;

import javax.swing.JButton;
import java.util.function.Consumer;

public class EmpleadoPanel extends PanelPersonalBase {
    private final SalasModulo salasModulo;
    private final VentasModulo ventasModulo;
    private final AvisosModulo avisosModulo;

    public EmpleadoPanel(ApiClient apiClient, Consumer<String> estado) {
        super(false);
        salasModulo = new SalasModulo(apiClient, true, this, estado);
        ventasModulo = new VentasModulo(apiClient, true, this, estado);
        avisosModulo = new AvisosModulo(apiClient, this, estado);

        agregarSeccion("Control de acceso", ventasModulo.panelTickets());
        agregarSeccion("Consumos", ventasModulo.panelConsumos());
        agregarSeccion("Funciones", salasModulo.panelFunciones());
        agregarSeccion("Salas", salasModulo.panelSalas());
        agregarSeccion("Butacas", salasModulo.panelButacas());
        finalizarMenu("Control de acceso");

        registrarModulo(salasModulo);
        registrarModulo(ventasModulo);
        registrarModulo(avisosModulo);
        recargar();
    }

    @Override
    public JButton botonAvisos() {
        return avisosModulo.boton();
    }
}
