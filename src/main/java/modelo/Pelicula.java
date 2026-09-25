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
    @Lob//campos que superan los límites de tamaño habituales de los tipos de datos estándar
    private String descripcion;
    @Lob
    private String portadaUrl;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
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
    }

    public void asignarId(int id) {
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
