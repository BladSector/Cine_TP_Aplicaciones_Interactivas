package desktop.duenio.modulos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import desktop.duenio.dialogos.Opcion;
import java.util.ArrayList;
import java.util.List;

public record DatosAdministracion(JsonNode categorias, JsonNode peliculas, JsonNode salas,
                                  JsonNode funciones, JsonNode productos) {
    public static DatosAdministracion vacios() {
        JsonNode vacio = JsonNodeFactory.instance.arrayNode();
        return new DatosAdministracion(vacio, vacio, vacio, vacio, vacio);
    }

    public List<Opcion> opcionesCategorias() {
        return opciones(categorias, "nombre");
    }

    public List<Opcion> opcionesPeliculas() {
        return opciones(peliculas, "titulo");
    }

    public List<Opcion> opcionesSalas() {
        return opciones(salas, "nombre");
    }

    private static List<Opcion> opciones(JsonNode registros, String nombre) {
        List<Opcion> opciones = new ArrayList<>();
        registros.forEach(registro -> opciones.add(
                new Opcion(registro.path("id").asInt(), registro.path(nombre).asText())));
        return opciones;
    }
}
