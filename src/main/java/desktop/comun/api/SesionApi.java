package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public final class SesionApi {
    private final ApiClient apiClient;

    public SesionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode iniciarSesion(String usuario, String contrasenia) throws IOException, InterruptedException {
        apiClient.post("/sesion/personal", Map.of("usuario", usuario, "contrasenia", contrasenia));
        return consultarSesion();
    }

    public JsonNode consultarSesion() throws IOException, InterruptedException {
        return apiClient.get("/sesion/personal");
    }

    public void cerrarSesion() throws IOException, InterruptedException {
        apiClient.delete("/sesion");
    }
}
