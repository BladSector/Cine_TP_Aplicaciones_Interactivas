package modelo;

import java.util.ArrayList;
import java.util.List;

public class Categoria {
    private int id;
    private String nombre;
    private List<Pelicula> peliculas;

    public Categoria(String nombre) {
        this.id = 0;
        this.nombre = nombre;
        this.peliculas = new ArrayList<>();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la categoria no son validos.");
        }
    }

    private boolean validarDatos() {
        return nombre != null && !nombre.isBlank();
    }

    public void agregarPelicula(Pelicula pelicula) {
        if (pelicula == null) {
            throw new IllegalArgumentException("La película no puede ser vacía.");
        }

        peliculas.add(pelicula);
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
