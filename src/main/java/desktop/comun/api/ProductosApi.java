package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public final class ProductosApi {
    private final ApiClient apiClient;

    public ProductosApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarProductos() throws IOException, InterruptedException {
        return apiClient.get("/productos-confiteria");
    }

    public JsonNode crearProducto(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/productos-confiteria", cuerpo);
    }

    public JsonNode actualizarProducto(int productoId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/productos-confiteria/" + productoId, cuerpo);
    }

    public void eliminarProducto(int productoId) throws IOException, InterruptedException {
        apiClient.delete("/productos-confiteria/" + productoId);
    }
}
