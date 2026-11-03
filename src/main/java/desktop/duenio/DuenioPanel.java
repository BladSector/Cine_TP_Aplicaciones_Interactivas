package desktop.duenio;

import desktop.comun.ApiClient;
import desktop.comun.PanelPersonalBase;
import desktop.comun.modulos.SalasModulo;
import desktop.comun.modulos.VentasModulo;
import desktop.duenio.modulos.ActividadModulo;
import desktop.duenio.modulos.PersonalModulo;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.util.function.Consumer;

public class DuenioPanel extends PanelPersonalBase {
    private final AdministracionPanel administracionPanel;
    private final SalasModulo salasModulo;
    private final VentasModulo ventasModulo;
    private final PersonalModulo personalModulo;
    private final ActividadModulo actividadModulo;

    public DuenioPanel(ApiClient apiClient, Consumer<String> estado) {
        super(true);
        JTabbedPane administracion = new JTabbedPane();
        administracionPanel = new AdministracionPanel(apiClient, administracion, estado);
        salasModulo = new SalasModulo(apiClient, false, this, estado);
        ventasModulo = new VentasModulo(apiClient, false, this, estado);
        personalModulo = new PersonalModulo(apiClient, this, estado);
        actividadModulo = new ActividadModulo(apiClient, this, estado);

        agregarGrupo("OPERACIÓN");
        agregarSeccion("Ventas y tickets", ventasModulo.panelTickets());
        agregarSeccion("Consumos", ventasModulo.panelConsumos());
        agregarGrupo("CARTELERA");
        agregarSeccion("Funciones", extraerPestania(administracion, "Funciones"));
        agregarSeccion("Películas", extraerPestania(administracion, "Películas"));
        agregarSeccion("Categorías", extraerPestania(administracion, "Categorías"));
        agregarGrupo("INSTALACIONES");
        agregarSeccion("Salas", extraerPestania(administracion, "Salas"));
        agregarSeccion("Butacas", salasModulo.panelButacas());
        agregarGrupo("CONFITERÍA");
        agregarSeccion("Productos", extraerPestania(administracion, "Confitería"));
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

    private JPanel extraerPestania(JTabbedPane origen, String titulo) {
        for (int indice = 0; indice < origen.getTabCount(); indice++) {
            if (titulo.equals(origen.getTitleAt(indice))) {
                Component contenido = origen.getComponentAt(indice);
                origen.removeTabAt(indice);
                return (JPanel) contenido;
            }
        }
        throw new IllegalStateException("No se encontró el módulo " + titulo + ".");
    }
}
