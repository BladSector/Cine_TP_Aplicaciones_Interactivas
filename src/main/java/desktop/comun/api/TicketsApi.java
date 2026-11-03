package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class TicketsApi {
    private final ApiClient apiClient;

    public TicketsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarTickets() throws IOException, InterruptedException {
        return apiClient.get("/tickets");
    }

    public JsonNode buscarPorId(int ticketId) throws IOException, InterruptedException {
        return apiClient.get("/tickets/" + ticketId);
    }

    public JsonNode buscarPorQr(String codigo) throws IOException, InterruptedException {
        String codigoCodificado = URLEncoder.encode(codigo, StandardCharsets.UTF_8).replace("+", "%20");
        return apiClient.get("/tickets/qr/" + codigoCodificado);
    }

    public JsonNode validarEntradas(int ticketId) throws IOException, InterruptedException {
        return apiClient.put("/tickets/" + ticketId + "/validar-entradas", null);
    }

    public JsonNode entregarConsumos(int ticketId) throws IOException, InterruptedException {
        return apiClient.put("/tickets/" + ticketId + "/entregar-consumos", null);
    }

    public JsonNode reembolsarTicket(int ticketId) throws IOException, InterruptedException {
        return apiClient.put("/tickets/" + ticketId + "/reembolsar", null);
    }

    public JsonNode listarConsumos() throws IOException, InterruptedException {
        return apiClient.get("/items-consumo");
    }

    public JsonNode entregarConsumo(int consumoId) throws IOException, InterruptedException {
        return apiClient.put("/items-consumo/" + consumoId + "/entregar", null);
    }
}
