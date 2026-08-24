package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

@Entity//Sirve para indicar que la siguiente clase forma parte de una de las tablas de la base de datos
@Table(name = "categoria")//Indica el nombre de la tabla. Si no se pone usa el nombre de la clase.
public class Categoria {
    @Id//Significa que este atributo "id" es la primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)//la base de datos genera el id automáticamente
    private int id;

    private String nombre;

    @OneToMany(mappedBy = "categoria")//Una Categoria puede tener muchas Pelicula, pero la relación se guarda del lado de Pelicula, en el atributo llamado categoria
    private List<Pelicula> peliculas;

    protected Categoria() {//El hibernate necesita constructores vacíos para crear tablas, es protected para no usarlo como contructor normal
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

    public void actualizarNombre(String nombre) {
        this.nombre = nombre;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la categoria no son validos.");
        }
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
