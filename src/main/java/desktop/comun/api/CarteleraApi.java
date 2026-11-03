package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public final class CarteleraApi {
    private final ApiClient apiClient;

    public CarteleraApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarCategorias() throws IOException, InterruptedException {
        return apiClient.get("/categorias");
    }

    public JsonNode crearCategoria(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/categorias", cuerpo);
    }

    public JsonNode actualizarCategoria(int id, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/categorias/" + id, cuerpo);
    }

    public void eliminarCategoria(int id) throws IOException, InterruptedException {
        apiClient.delete("/categorias/" + id);
    }

    public JsonNode listarPeliculas() throws IOException, InterruptedException {
        return apiClient.get("/peliculas");
    }

    public JsonNode crearPelicula(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/peliculas", cuerpo);
    }

    public JsonNode actualizarPelicula(int id, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/peliculas/" + id, cuerpo);
    }

    public void eliminarPelicula(int id) throws IOException, InterruptedException {
        apiClient.delete("/peliculas/" + id);
    }

    public JsonNode listarFunciones() throws IOException, InterruptedException {
        return apiClient.get("/funciones");
    }

    public JsonNode crearFuncion(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/funciones", cuerpo);
    }

    public JsonNode actualizarFuncion(int id, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/funciones/" + id, cuerpo);
    }

    public void eliminarFuncion(int id) throws IOException, InterruptedException {
        apiClient.delete("/funciones/" + id);
    }

    public JsonNode consultarEntradasActivas(int funcionId) throws IOException, InterruptedException {
        return apiClient.get("/funciones/" + funcionId + "/entradas-activas");
    }

    public JsonNode actualizarHorario(int funcionId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/funciones/" + funcionId + "/horario", cuerpo);
    }
}
