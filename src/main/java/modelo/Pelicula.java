package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// o import jakarta.persistence.*;

@Entity
@Table(name = "pelicula")
public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String titulo;
    private int duracion;
    @Lob
    private String descripcion;
    @Lob
    private String portadaUrl;
    @ManyToOne//Muchas películas pueden pertenecer a una misma categoría
    @JoinColumn(name = "categoria_id")//En la tabla pelicula, MySQL va a guardar una columna llamada categoria_id, que apunta al id de la tabla categoria.
    private Categoria categoria;

    protected Pelicula(){
    }

    public Pelicula(String titulo, int duracion, Categoria categoria) {
        this(titulo, duracion, "", "", categoria);
    }

    public Pelicula(String titulo, int duracion, String descripcion, String portadaUrl, Categoria categoria) {
        this.id = 0;
        this.titulo = titulo;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.portadaUrl = portadaUrl;
        this.categoria = categoria;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la pelicula no son validos.");
        }
        //Acá agrega la película a la lista de películas de una categoría con metodo agregarPelicula() en categoría
        this.categoria.agregarPelicula(this);
    }

    private boolean validarDatos() {
        return titulo != null
                && duracion > 0
                && categoria != null;
    }

    public void actualizarDatos(String titulo, int duracion, Categoria categoria) {
        actualizarDatos(titulo, duracion, descripcion, portadaUrl, categoria);
    }

    public void actualizarDatos(String titulo, int duracion, String descripcion, String portadaUrl, Categoria categoria) {
        this.titulo = titulo;
        this.duracion = duracion;
        this.descripcion = descripcion;
        this.portadaUrl = portadaUrl;
        this.categoria = categoria;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la pelicula no son validos.");
        }
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getPortadaUrl() {
        return portadaUrl;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
