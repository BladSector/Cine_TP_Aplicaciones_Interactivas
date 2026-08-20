package modelo;

public class ProductoConfiteria {
    private int id;
    private String nombre;
    private double precio;
    private TipoProductoConfiteria tipo;
    private TamanoProductoConfiteria tamano;

    public ProductoConfiteria(String nombre, double precio,
                              TipoProductoConfiteria tipo,
                              TamanoProductoConfiteria tamano) {
        this.id = 0;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.tamano = tamano;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del producto de confiteria no son validos.");
        }
    }

    private boolean validarDatos() {
        return nombre != null && !nombre.isBlank()
                && precio > 0
                && tipo != null
                && tamano != null;
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

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public TipoProductoConfiteria getTipo() {
        return tipo;
    }

    public TamanoProductoConfiteria getTamano() {
        return tamano;
    }
}
