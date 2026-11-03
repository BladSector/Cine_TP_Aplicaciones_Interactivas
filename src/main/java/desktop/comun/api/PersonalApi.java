package desktop.comun.api;

import desktop.comun.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

public final class PersonalApi {
    private final ApiClient apiClient;

    public PersonalApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JsonNode listarEmpleados() throws IOException, InterruptedException {
        return apiClient.get("/empleados");
    }

    public JsonNode listarEmpleadosActivos() throws IOException, InterruptedException {
        return apiClient.get("/empleados/activos");
    }

    public JsonNode crearEmpleado(Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.post("/empleados", cuerpo);
    }

    public JsonNode activarEmpleado(int empleadoId) throws IOException, InterruptedException {
        return apiClient.put("/empleados/" + empleadoId + "/activar", null);
    }

    public JsonNode desactivarEmpleado(int empleadoId) throws IOException, InterruptedException {
        return apiClient.put("/empleados/" + empleadoId + "/desactivar", null);
    }

    public JsonNode restablecerContrasenia(int empleadoId, Map<String, Object> cuerpo) throws IOException, InterruptedException {
        return apiClient.put("/empleados/" + empleadoId + "/contrasenia", cuerpo);
    }
}
