package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public final class AuditoriaApi {
    private final ApiClient apiClient;

    public AuditoriaApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarActividad() throws IOException, InterruptedException {
        return apiClient.get("/auditorias");
    }
}
