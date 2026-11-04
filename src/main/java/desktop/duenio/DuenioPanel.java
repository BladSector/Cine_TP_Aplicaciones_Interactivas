package desktop.duenio;

import desktop.comun.ApiClient;
import desktop.comun.PanelPersonalBase;
import desktop.comun.modulos.SalasModulo;
import desktop.comun.modulos.VentasModulo;
import desktop.duenio.modulos.ActividadModulo;
import desktop.duenio.modulos.PersonalModulo;

import java.util.function.Consumer;

public class DuenioPanel extends PanelPersonalBase {
    private final AdministracionPanel administracionPanel;
    private final SalasModulo salasModulo;
    private final VentasModulo ventasModulo;
    private final PersonalModulo personalModulo;
    private final ActividadModulo actividadModulo;

    public DuenioPanel(ApiClient apiClient, Consumer<String> estado) {
        super(true);
        administracionPanel = new AdministracionPanel(apiClient, estado);
        salasModulo = new SalasModulo(apiClient, false, this, estado);
        ventasModulo = new VentasModulo(apiClient, false, this, estado);
        personalModulo = new PersonalModulo(apiClient, this, estado);
        actividadModulo = new ActividadModulo(apiClient, this, estado);

        agregarGrupo("OPERACIÓN");
        agregarSeccion("Ventas y tickets", ventasModulo.panelTickets());
        agregarSeccion("Consumos", ventasModulo.panelConsumos());
        agregarGrupo("CARTELERA");
        agregarSeccion("Funciones", administracionPanel.panelFunciones());
        agregarSeccion("Películas", administracionPanel.panelPeliculas());
        agregarSeccion("Categorías", administracionPanel.panelCategorias());
        agregarGrupo("INSTALACIONES");
        agregarSeccion("Salas", administracionPanel.panelSalas());
        agregarSeccion("Butacas", salasModulo.panelButacas());
        agregarGrupo("CONFITERÍA");
        agregarSeccion("Productos", administracionPanel.panelProductos());
        agregarGrupo("ADMINISTRACIÓN");
        agregarSeccion("Empleados", personalModulo.panel());
        agregarSeccion("Actividad", actividadModulo.panel());
        finalizarMenu("Funciones");

        registrarCicloDeVida(administracionPanel::recargar, administracionPanel::detener);
        registrarModulo(salasModulo);
        registrarModulo(ventasModulo);
        registrarModulo(personalModulo);
        registrarModulo(actividadModulo);
        recargar();
    }

}
