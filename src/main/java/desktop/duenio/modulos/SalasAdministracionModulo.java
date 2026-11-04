package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.ApiClient;
import desktop.comun.api.PersonalApi;
import desktop.comun.api.SalasApi;
import desktop.comun.dialogos.EstadoSalaDialog;
import desktop.duenio.dialogos.SalaDialog;
import desktop.duenio.paneles.SalasAdminPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JPanel;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.itemHtml;
import static desktop.comun.util.DetalleHtml.mensajeSecundarioHtml;
import static desktop.comun.util.DetalleHtml.seccionHtml;
import static desktop.comun.util.DetalleHtml.tituloHtml;
import static desktop.comun.util.FormatoSwing.nombreEstadoSala;
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public final class SalasAdministracionModulo extends CrudModulo {
    private final SalasApi api;
    private final PersonalApi personalApi;
    private final JPanel panel;

    public SalasAdministracionModulo(ApiClient cliente, Component padre, Consumer<String> estado, Runnable recargarTodo) {
        super(padre, estado, recargarTodo, "la sala", "ID", "Nombre", "Capacidad", "Estado", "Detalle");
        api = new SalasApi(cliente);
        personalApi = new PersonalApi(cliente);
        panel = new SalasAdminPanel(tabla, this::nuevo, this::recargar, () -> mostrarDetalle("Salas"));
    }

    @Override public JPanel panel() { return panel; }
    @Override protected JsonNode registros() { return datos.salas(); }

    @Override
    protected void agregarFila(JsonNode sala) {
        String detalle = sala.path("detalleEstado").asText("");
        modeloTabla.addRow(new Object[]{sala.path("id").asInt(), sala.path("nombre").asText(),
                sala.path("capacidad").asInt(), nombreEstadoSala(sala.path("estado").asText("DISPONIBLE")),
                detalle.isBlank() ? "-" : detalle});
    }

    @Override
    protected List<AccionDetalle> acciones(JsonNode sala) {
        return List.of(new AccionDetalle("Editar", () -> formulario(sala)),
                new AccionDetalle("Cambiar estado", () -> cambiarEstado(sala)),
                new AccionDetalle("Eliminar", () -> confirmarEliminacion(sala)));
    }

    @Override
    protected void formulario(JsonNode sala) {
        if (sala == null) {
            SalaDialog.mostrarNueva(padre).ifPresent(formulario -> {
                Map<String, Object> cuerpo = Map.of("nombre", formulario.nombre(), "butacas", formulario.butacas());
                ejecutar("Creando sala y butacas...", () -> api.crearSala(cuerpo), respuesta -> recargar());
            });
            return;
        }
        int id = sala.path("id").asInt();
        String nombre = sala.path("nombre").asText();
        ejecutar("Cargando distribución...", api::listarButacas, respuesta -> {
            List<JsonNode> butacas = new ArrayList<>();
            respuesta.forEach(butaca -> {
                if (butaca.path("salaId").asInt() == id) butacas.add(butaca);
            });
            SalaDialog.mostrarEdicion(padre, nombre, butacas).ifPresent(formulario -> {
                Map<String, Object> cuerpo = Map.of("nombre", formulario.nombre(), "butacas", formulario.butacas());
                ejecutar("Actualizando distribución...", () -> api.actualizarDistribucion(id, cuerpo),
                        respuestaApi -> recargar());
            });
        });
    }

    private void cambiarEstado(JsonNode sala) {
        int id = sala.path("id").asInt();
        String estado = nombreEstadoSala(sala.path("estado").asText("DISPONIBLE"));
        ejecutar("Cargando empleados...", personalApi::listarEmpleadosActivos, empleados ->
                EstadoSalaDialog.mostrar(padre, estado, empleados).ifPresent(formulario -> {
                    Map<String, Object> cuerpo = new HashMap<>();
                    cuerpo.put("estado", formulario.estado());
                    cuerpo.put("descripcion", formulario.descripcion());
                    if (formulario.destinatarioEmpleadoId() != null) {
                        cuerpo.put("destinatarioEmpleadoId", formulario.destinatarioEmpleadoId());
                    }
                    ejecutar("Actualizando estado de la sala...", () -> api.cambiarEstado(id, cuerpo),
                            respuesta -> recargar());
                }));
    }

    @Override protected void eliminarRegistro(int id) throws Exception { api.eliminarSala(id); }

    @Override
    protected String descripcion(JsonNode sala) {
        String detalle = sala.path("detalleEstado").asText("");
        StringBuilder html = new StringBuilder(inicioHtml()).append(tituloHtml("SALA"))
                .append(campoHtml("Nombre", sala.path("nombre").asText()))
                .append(campoHtml("Capacidad", sala.path("capacidad").asInt()))
                .append(campoHtml("Estado", nombreEstadoSala(sala.path("estado").asText("DISPONIBLE"))))
                .append(campoHtml("Detalle", detalle.isBlank() ? "-" : detalle))
                .append(seccionHtml("FUNCIONES PROGRAMADAS"));
        int cantidad = 0;
        for (JsonNode funcion : datos.funciones()) {
            if (funcion.path("salaId").asInt() != sala.path("id").asInt()) continue;
            cantidad++;
            html.append(itemHtml(funcion.path("peliculaTitulo").asText()))
                    .append(campoHtml("Fecha", funcion.path("fecha").asText()))
                    .append(campoHtml("Horario", funcion.path("horario").asText()))
                    .append(campoHtml("Formato", nombreFormato(funcion.path("formato").asText())))
                    .append(campoHtml("Idioma", nombreIdioma(funcion.path("idioma").asText())))
                    .append(campoHtml("Precio", "$" + funcion.path("precioEntrada").asDouble())).append("<hr>");
        }
        if (cantidad == 0) html.append(mensajeSecundarioHtml("No hay funciones programadas en esta sala."));
        return html.append("</body></html>").toString();
    }
}
