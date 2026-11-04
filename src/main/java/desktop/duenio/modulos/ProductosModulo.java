package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.ApiClient;
import desktop.comun.api.ProductosApi;
import desktop.duenio.dialogos.ProductoDialog;
import desktop.duenio.paneles.ProductosPanel;
import java.awt.Component;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JPanel;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;

public final class ProductosModulo extends CrudModulo {
    private final ProductosApi api;
    private final JPanel panel;

    public ProductosModulo(ApiClient cliente, Component padre, Consumer<String> estado, Runnable recargarTodo) {
        super(padre, estado, recargarTodo, "el producto", "ID", "Nombre", "Precio", "Tipo", "Tamaño", "Descripción", "Imagen");
        api = new ProductosApi(cliente);
        panel = new ProductosPanel(tabla, this::nuevo, this::recargar, () -> mostrarDetalle("Productos de confitería"));
    }

    @Override public JPanel panel() { return panel; }
    @Override protected JsonNode registros() { return datos.productos(); }

    @Override
    protected void agregarFila(JsonNode producto) {
        modeloTabla.addRow(new Object[]{producto.path("id").asInt(), producto.path("nombre").asText(),
                producto.path("precio").asDouble(), producto.path("tipo").asText(), producto.path("tamano").asText(),
                producto.path("descripcion").asText(""), imagen(producto)});
    }

    @Override
    protected void formulario(JsonNode producto) {
        boolean edicion = producto != null;
        int id = edicion ? producto.path("id").asInt() : 0;
        ProductoDialog.Datos inicial = edicion ? new ProductoDialog.Datos(producto.path("nombre").asText(),
                producto.path("descripcion").asText(""), producto.path("precio").asDouble(),
                producto.path("tipo").asText(), producto.path("tamano").asText(),
                producto.path("imagenUrl").asText("")) : null;
        ProductoDialog.mostrar(padre, inicial).ifPresent(formulario -> {
            Map<String, Object> cuerpo = Map.of("nombre", formulario.nombre(), "descripcion", formulario.descripcion(),
                    "precio", formulario.precio(), "tipo", formulario.tipo(), "tamano", formulario.tamano(),
                    "imagenUrl", formulario.imagenUrl());
            guardar(edicion, "producto", () -> edicion
                    ? api.actualizarProducto(id, cuerpo) : api.crearProducto(cuerpo));
        });
    }

    @Override protected void eliminarRegistro(int id) throws Exception { api.eliminarProducto(id); }

    @Override
    protected String descripcion(JsonNode producto) {
        return inicioHtml() + campoHtml("ID", producto.path("id").asInt())
                + campoHtml("Nombre", producto.path("nombre").asText())
                + campoHtml("Precio", producto.path("precio").asDouble())
                + campoHtml("Tipo", producto.path("tipo").asText())
                + campoHtml("Tamaño", producto.path("tamano").asText())
                + campoHtml("Descripción", producto.path("descripcion").asText(""))
                + campoHtml("Imagen", imagen(producto)) + "</body></html>";
    }

    private static String imagen(JsonNode producto) {
        return producto.path("imagenUrl").asText("").isBlank() ? "Sin imagen" : "Cargada";
    }
}
