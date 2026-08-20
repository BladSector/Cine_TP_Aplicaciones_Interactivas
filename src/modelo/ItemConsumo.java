package modelo;

public class ItemConsumo {
    private ProductoConfiteria producto;
    private int cantidad;

    public ItemConsumo(ProductoConfiteria producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del item de consumo no son validos.");
        }
    }

    private boolean validarDatos() {
        return producto != null
                && cantidad > 0;
    }

    public double calcularSubtotal() {
        return producto.getPrecio() * cantidad;
    }

    public ProductoConfiteria getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }
}
