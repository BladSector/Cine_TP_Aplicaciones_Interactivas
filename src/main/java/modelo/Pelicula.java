package modelo;

public class Pelicula {
    private int id;
    private String titulo;
    private int duracion;
    private Categoria categoria;

    public Pelicula(String titulo, int duracion, Categoria categoria) {
        this.id = 0;
        this.titulo = titulo;
        this.duracion = duracion;
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

    public Categoria getCategoria() {
        return categoria;
    }
}
