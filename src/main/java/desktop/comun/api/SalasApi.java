package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public final class SalasApi {
    private final ApiClient apiClient;

    public SalasApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarSalas() throws IOException, InterruptedException {
        return apiClient.get("/salas");
    }

    public JsonNode crearSala(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/salas/con-matriz", cuerpo);
    }

    public JsonNode actualizarDistribucion(int salaId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/salas/" + salaId + "/distribucion", cuerpo);
    }

    public JsonNode cambiarEstado(int salaId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/salas/" + salaId + "/estado", cuerpo);
    }

    public void eliminarSala(int salaId) throws IOException, InterruptedException {
        apiClient.delete("/salas/" + salaId);
    }

    public JsonNode listarButacas() throws IOException, InterruptedException {
        return apiClient.get("/butacas");
    }

    public JsonNode ocuparButaca(int butacaId) throws IOException, InterruptedException {
        return apiClient.put("/butacas/" + butacaId + "/ocupar", null);
    }

    public JsonNode liberarButaca(int butacaId) throws IOException, InterruptedException {
        return apiClient.put("/butacas/" + butacaId + "/liberar", null);
    }

    public JsonNode habilitarButaca(int butacaId) throws IOException, InterruptedException {
        return apiClient.put("/butacas/" + butacaId + "/habilitar", null);
    }

    public JsonNode marcarButacaFueraDeServicio(int butacaId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/butacas/" + butacaId + "/fuera-de-servicio", cuerpo);
    }

    public JsonNode listarAvisos() throws IOException, InterruptedException {
        return apiClient.get("/avisos-sala");
    }

    public JsonNode listarAvisosPendientes() throws IOException, InterruptedException {
        return apiClient.get("/avisos-sala/pendientes");
    }

    public JsonNode marcarAvisoLeido(long avisoId) throws IOException, InterruptedException {
        return apiClient.put("/avisos-sala/" + avisoId + "/leer", null);
    }
}
