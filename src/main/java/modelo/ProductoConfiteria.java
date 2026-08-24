package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto_confiteria")
public class ProductoConfiteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private double precio;
    @Enumerated(EnumType.STRING)
    private TipoProductoConfiteria tipo;
    @Enumerated(EnumType.STRING)
    private TamanoProductoConfiteria tamano;

    protected ProductoConfiteria() {
    }

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

    public void actualizarDatos(String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.tamano = tamano;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos del producto de confiteria no son validos.");
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
