package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id//Significa que este atributo "id" es la primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;

    @OneToMany(mappedBy = "categoria")
    private List<Pelicula> peliculas;

    protected Categoria() {//El hibernate necesita constructores vacíos para crear tablas, es protected para no usarlo como contructor normal
        this.peliculas = new ArrayList<>();
    }

    public Categoria(String nombre) {
        this.id = 0;
        this.nombre = nombre;
        this.peliculas = new ArrayList<>();
    }

    public void actualizarNombre(String nombre) {
        this.nombre = nombre;
    }

    public void agregarPelicula(Pelicula pelicula) {
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
