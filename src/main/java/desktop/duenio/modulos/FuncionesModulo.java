package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import desktop.comun.ApiClient;
import desktop.comun.api.CarteleraApi;
import desktop.duenio.dialogos.FuncionDialog;
import desktop.duenio.paneles.FuncionesAdminPanel;
import java.awt.Component;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public final class FuncionesModulo extends CrudModulo {
    private final CarteleraApi api;
    private final JPanel panel;

    public FuncionesModulo(ApiClient cliente, Component padre, Consumer<String> estado, Runnable recargarTodo) {
        super(padre, estado, recargarTodo, "la función", "ID", "Fecha", "Horario", "Película ID",
                "Película", "Sala ID", "Sala", "Formato", "Idioma", "Precio");
        api = new CarteleraApi(cliente);
        panel = new FuncionesAdminPanel(tabla, this::nuevo, this::recargar, () -> mostrarDetalle("Funciones"));
    }

    @Override public JPanel panel() { return panel; }
    @Override protected JsonNode registros() { return datos.funciones(); }

    @Override
    protected void agregarFila(JsonNode funcion) {
        modeloTabla.addRow(new Object[]{funcion.path("id").asInt(), funcion.path("fecha").asText(),
                funcion.path("horario").asText(), funcion.path("peliculaId").asInt(),
                funcion.path("peliculaTitulo").asText(), funcion.path("salaId").asInt(),
                funcion.path("salaNombre").asText(), nombreFormato(funcion.path("formato").asText()),
                nombreIdioma(funcion.path("idioma").asText()), funcion.path("precioEntrada").asDouble()});
    }

    @Override
    protected void formulario(JsonNode funcion) {
        if (funcion == null) {
            mostrarFormulario(null, null);
            return;
        }
        int id = funcion.path("id").asInt();
        FuncionDialog.Datos inicial = new FuncionDialog.Datos(funcion.path("fecha").asText(),
                funcion.path("horario").asText(), funcion.path("peliculaId").asInt(),
                funcion.path("salaId").asInt(), funcion.path("formato").asText(),
                funcion.path("idioma").asText(), funcion.path("precioEntrada").asDouble());
        ejecutar("Comprobando entradas vendidas...", () -> api.consultarEntradasActivas(id), respuesta -> {
            long cantidad = respuesta.path("cantidadEntradas").asLong();
            if (cantidad > 0) {
                int confirmacion = JOptionPane.showConfirmDialog(padre,
                        "Esta función tiene " + cantidad + " entrada(s) vendida(s).\n"
                                + "Modificar sus datos puede afectar a los espectadores.\n\n"
                                + "¿Querés continuar?", "Función con entradas vendidas",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmacion != JOptionPane.YES_OPTION) return;
            }
            mostrarFormulario(id, inicial);
        });
    }

    private void mostrarFormulario(Integer id, FuncionDialog.Datos inicial) {
        if (datos.peliculas().isEmpty() || datos.salas().isEmpty()) {
            mostrarAviso("Primero deben existir Películas y Salas.");
            return;
        }
        FuncionDialog.mostrar(padre, inicial, datos.opcionesPeliculas(), datos.opcionesSalas()).ifPresent(formulario -> {
            Map<String, Object> cuerpo = Map.of("fecha", formulario.fecha(), "horario", formulario.horario(),
                    "peliculaId", formulario.peliculaId(), "salaId", formulario.salaId(),
                    "formato", formulario.formato(), "idioma", formulario.idioma(),
                    "precioEntrada", formulario.precioEntrada());
            guardar(id != null, "función", () -> id != null
                    ? api.actualizarFuncion(id, cuerpo) : api.crearFuncion(cuerpo));
        });
    }

    @Override protected void eliminarRegistro(int id) throws Exception { api.eliminarFuncion(id); }

    @Override
    protected String descripcion(JsonNode funcion) {
        return inicioHtml()
                + campoHtml("ID", funcion.path("id").asInt())
                + campoHtml("Fecha", funcion.path("fecha").asText())
                + campoHtml("Horario", funcion.path("horario").asText())
                + campoHtml("Película ID", funcion.path("peliculaId").asInt())
                + campoHtml("Película", funcion.path("peliculaTitulo").asText())
                + campoHtml("Sala ID", funcion.path("salaId").asInt())
                + campoHtml("Sala", funcion.path("salaNombre").asText())
                + campoHtml("Formato", nombreFormato(funcion.path("formato").asText()))
                + campoHtml("Idioma", nombreIdioma(funcion.path("idioma").asText()))
                + campoHtml("Precio", funcion.path("precioEntrada").asDouble()) + "</body></html>";
    }
}
