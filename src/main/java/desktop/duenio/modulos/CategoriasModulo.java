package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.duenio.dialogos.CategoriaDialog;
import desktop.duenio.paneles.CategoriasPanel;
import java.awt.Component;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JPanel;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.itemHtml;
import static desktop.comun.util.DetalleHtml.mensajeSecundarioHtml;
import static desktop.comun.util.DetalleHtml.seccionHtml;
import static desktop.comun.util.DetalleHtml.tituloHtml;

public final class CategoriasModulo extends CrudModulo {
    private final CarteleraApi api;
    private final JPanel panel;

    public CategoriasModulo(ApiClient cliente, Component padre, Consumer<String> estado, Runnable recargarTodo) {
        super(padre, estado, recargarTodo, "la categoría", "ID", "Nombre");
        api = new CarteleraApi(cliente);
        panel = new CategoriasPanel(tabla, this::nuevo, this::recargar, () -> mostrarDetalle("Categorías"));
    }

    @Override public JPanel panel() { return panel; }
    @Override protected JsonNode registros() { return datos.categorias(); }

    @Override
    protected void agregarFila(JsonNode registro) {
        modeloTabla.addRow(new Object[]{registro.path("id").asInt(), registro.path("nombre").asText()});
    }

    @Override
    protected void formulario(JsonNode registro) {
        boolean edicion = registro != null;
        int id = edicion ? registro.path("id").asInt() : 0;
        String nombre = edicion ? registro.path("nombre").asText() : "";
        CategoriaDialog.mostrar(padre, nombre).ifPresent(datos -> {
            Map<String, Object> cuerpo = Map.of("nombre", datos.nombre());
            guardar(edicion, "categoría", () -> edicion
                    ? api.actualizarCategoria(id, cuerpo) : api.crearCategoria(cuerpo));
        });
    }

    @Override protected void eliminarRegistro(int id) throws Exception { api.eliminarCategoria(id); }

    @Override
    protected String descripcion(JsonNode categoria) {
        StringBuilder html = new StringBuilder(inicioHtml()).append(tituloHtml("CATEGORÍA"))
                .append(campoHtml("Nombre", categoria.path("nombre").asText()))
                .append(seccionHtml("PELÍCULAS ASOCIADAS"));
        int cantidad = 0;
        for (JsonNode pelicula : datos.peliculas()) {
            if (pelicula.path("categoriaId").asInt() != categoria.path("id").asInt()) continue;
            cantidad++;
            html.append(itemHtml(pelicula.path("titulo").asText()))
                    .append(campoHtml("ID", pelicula.path("id").asInt()))
                    .append(campoHtml("Duración", pelicula.path("duracion").asInt() + " min")).append("<hr>");
        }
        if (cantidad == 0) html.append(mensajeSecundarioHtml("No hay películas asociadas a esta categoría."));
        return html.append("</body></html>").toString();
    }
}
