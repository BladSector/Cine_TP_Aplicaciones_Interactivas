public class Pelicula {
    private String titulo;
    private int duracion;
    private Categoria categoria;

    public Pelicula(String titulo, int duracion, Categoria categoria) {
        this.titulo = titulo;
        this.duracion = duracion;
        this.categoria = categoria;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la pelicula no son validos.");
        }
        //Acá agrega la película a la lista de películas de una categoría
        this.categoria.agregarPelicula(this);
    }

    private boolean validarDatos() {
        return titulo != null
                && duracion > 0
                && categoria != null;
    }
}
