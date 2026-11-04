package desktop.duenio;

import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.comun.api.ProductosApi;
import desktop.comun.api.SalasApi;
import desktop.comun.modulos.ModuloSwing;
import desktop.duenio.modulos.CategoriasModulo;
import desktop.duenio.modulos.CrudModulo;
import desktop.duenio.modulos.DatosAdministracion;
import desktop.duenio.modulos.FuncionesModulo;
import desktop.duenio.modulos.PeliculasModulo;
import desktop.duenio.modulos.ProductosModulo;
import desktop.duenio.modulos.SalasAdministracionModulo;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

public class AdministracionPanel extends JPanel {
    private final CategoriasModulo categoriasModulo;
    private final PeliculasModulo peliculasModulo;
    private final SalasAdministracionModulo salasModulo;
    private final FuncionesModulo funcionesModulo;
    private final ProductosModulo productosModulo;
    private final List<CrudModulo> modulos;
    private final ModuloSwing cargador;

    public AdministracionPanel(ApiClient cliente, Consumer<String> estado) {
        categoriasModulo = new CategoriasModulo(cliente, this, estado, this::recargar);
        peliculasModulo = new PeliculasModulo(cliente, this, estado, this::recargar);
        salasModulo = new SalasAdministracionModulo(cliente, this, estado, this::recargar);
        funcionesModulo = new FuncionesModulo(cliente, this, estado, this::recargar);
        productosModulo = new ProductosModulo(cliente, this, estado, this::recargar);
        modulos = List.of(categoriasModulo, peliculasModulo, salasModulo, funcionesModulo, productosModulo);
        CarteleraApi cartelera = new CarteleraApi(cliente);
        SalasApi salas = new SalasApi(cliente);
        ProductosApi productos = new ProductosApi(cliente);
        cargador = new ModuloSwing(this, estado) {
            @Override
            public void recargar() {
                consultar("administracion", "Cargando datos administrativos...", () -> new DatosAdministracion(
                        cartelera.listarCategorias(), cartelera.listarPeliculas(), salas.listarSalas(),
                        cartelera.listarFunciones(), productos.listarProductos()),
                        datos -> modulos.forEach(modulo -> modulo.aplicarDatos(datos)));
            }
        };
    }

    public JPanel panelCategorias() { return categoriasModulo.panel(); }
    public JPanel panelPeliculas() { return peliculasModulo.panel(); }
    public JPanel panelSalas() { return salasModulo.panel(); }
    public JPanel panelFunciones() { return funcionesModulo.panel(); }
    public JPanel panelProductos() { return productosModulo.panel(); }

    public void recargar() {
        cargador.recargar();
    }

    public void detener() {
        cargador.detener();
        modulos.forEach(CrudModulo::detener);
    }
}
