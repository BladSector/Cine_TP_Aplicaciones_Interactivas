package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    @Transient
    private List<Pelicula> peliculas;

    protected Categoria() {
        this.peliculas = new ArrayList<>();
    }

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
