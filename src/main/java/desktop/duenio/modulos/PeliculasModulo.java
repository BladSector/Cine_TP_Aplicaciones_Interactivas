package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.duenio.dialogos.PeliculaDialog;
import desktop.duenio.paneles.PeliculasPanel;
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
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public final class PeliculasModulo extends CrudModulo {
    private final CarteleraApi api;
    private final JPanel panel;

    public PeliculasModulo(ApiClient cliente, Component padre, Consumer<String> estado, Runnable recargarTodo) {
        super(padre, estado, recargarTodo, "la película", "ID", "Título", "Duración", "Categoría", "Descripción", "Portada");
        api = new CarteleraApi(cliente);
        panel = new PeliculasPanel(tabla, this::nuevo, this::recargar, () -> mostrarDetalle("Películas"));
    }

    @Override public JPanel panel() { return panel; }
    @Override protected JsonNode registros() { return datos.peliculas(); }

    @Override
    protected void agregarFila(JsonNode pelicula) {
        modeloTabla.addRow(new Object[]{pelicula.path("id").asInt(), pelicula.path("titulo").asText(),
                pelicula.path("duracion").asInt(), pelicula.path("categoriaNombre").asText(),
                pelicula.path("descripcion").asText(), portada(pelicula)});
    }

    @Override
    protected void formulario(JsonNode pelicula) {
        if (datos.categorias().isEmpty()) {
            mostrarAviso("Primero creá una Categoría.");
            return;
        }
        boolean edicion = pelicula != null;
        int id = edicion ? pelicula.path("id").asInt() : 0;
        PeliculaDialog.Datos inicial = edicion ? new PeliculaDialog.Datos(
                pelicula.path("titulo").asText(), pelicula.path("duracion").asInt(),
                pelicula.path("descripcion").asText(), pelicula.path("portadaUrl").asText(""),
                pelicula.path("categoriaId").asInt(), pelicula.path("categoriaNombre").asText()) : null;
        PeliculaDialog.mostrar(padre, inicial, datos.opcionesCategorias()).ifPresent(formulario -> {
            Map<String, Object> cuerpo = Map.of("titulo", formulario.titulo(), "duracion", formulario.duracion(),
                    "descripcion", formulario.descripcion(), "portadaUrl", formulario.portadaUrl(),
                    "categoriaId", formulario.categoriaId());
            guardar(edicion, "película", () -> edicion
                    ? api.actualizarPelicula(id, cuerpo) : api.crearPelicula(cuerpo));
        });
    }

    @Override protected void eliminarRegistro(int id) throws Exception { api.eliminarPelicula(id); }

    @Override
    protected String descripcion(JsonNode pelicula) {
        StringBuilder html = new StringBuilder(inicioHtml()).append(tituloHtml("PELÍCULA"))
                .append(campoHtml("Título", pelicula.path("titulo").asText()))
                .append(campoHtml("Descripción", pelicula.path("descripcion").asText()))
                .append(campoHtml("Duración", pelicula.path("duracion").asInt() + " min"))
                .append(campoHtml("Categoría", pelicula.path("categoriaNombre").asText()))
                .append(campoHtml("Portada", portada(pelicula))).append(seccionHtml("FUNCIONES ASOCIADAS"));
        int cantidad = 0;
        for (JsonNode funcion : datos.funciones()) {
            if (funcion.path("peliculaId").asInt() != pelicula.path("id").asInt()) continue;
            cantidad++;
            html.append(itemHtml(funcion.path("fecha").asText() + " - " + funcion.path("horario").asText()))
                    .append(campoHtml("Sala", funcion.path("salaNombre").asText()))
                    .append(campoHtml("Formato", nombreFormato(funcion.path("formato").asText())))
                    .append(campoHtml("Idioma", nombreIdioma(funcion.path("idioma").asText())))
                    .append(campoHtml("Precio", "$" + funcion.path("precioEntrada").asDouble())).append("<hr>");
        }
        if (cantidad == 0) html.append(mensajeSecundarioHtml("No hay funciones programadas para esta película."));
        return html.append("</body></html>").toString();
    }

    private static String portada(JsonNode pelicula) {
        return pelicula.path("portadaUrl").asText("").isBlank() ? "Sin portada" : "Cargada";
    }
}
