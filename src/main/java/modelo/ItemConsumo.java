package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_consumo")
public class ItemConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoConfiteria producto;
    private int cantidad;

    protected ItemConsumo() {
    }

    public ItemConsumo(ProductoConfiteria producto, int cantidad) {
        this.id = 0;
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

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public int getId() {
        return id;
    }

    public ProductoConfiteria getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }
}
