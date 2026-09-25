package desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient(String baseUrl) {
        String urlNormalizada = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.baseUri = URI.create(urlNormalizada);
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public JsonNode get(String ruta) throws IOException, InterruptedException {
        return enviar("GET", ruta, null);
    }

    public JsonNode post(String ruta, Object cuerpo) throws IOException, InterruptedException {
        return enviar("POST", ruta, cuerpo);
    }

    public JsonNode put(String ruta, Object cuerpo) throws IOException, InterruptedException {
        return enviar("PUT", ruta, cuerpo);
    }

    public void delete(String ruta) throws IOException, InterruptedException {
        enviar("DELETE", ruta, null);
    }

    public String formatear(JsonNode nodo) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodo);
        } catch (IOException e) {
            return nodo.toString();
        }
    }

    private JsonNode enviar(String metodo, String ruta, Object cuerpo) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher publicador = cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(cuerpo));

        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve(quitarBarraInicial(ruta)))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(metodo, publicador)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ApiException(response.statusCode(), obtenerMensaje(response.body()));
        }
        if (response.body() == null || response.body().isBlank()) {
            return objectMapper.nullNode();
        }
        return objectMapper.readTree(response.body());
    }

    private String obtenerMensaje(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return "La API no devolvio detalles del error.";
        }
        try {
            JsonNode respuesta = objectMapper.readTree(cuerpo);
            if (respuesta.hasNonNull("message")) {
                return respuesta.get("message").asText();
            }
            if (respuesta.hasNonNull("error") && respuesta.get("error").isTextual()) {
                return respuesta.get("error").asText();
            }
        } catch (IOException ignored) {
        }
        return cuerpo;
    }

    private String quitarBarraInicial(String ruta) {
        return ruta.startsWith("/") ? ruta.substring(1) : ruta;
    }

    public static class ApiException extends RuntimeException {
        private final int status;

        public ApiException(int status, String mensaje) {
            super(mensaje);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
