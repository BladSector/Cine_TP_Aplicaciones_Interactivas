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
    }

    public void actualizarDatos(String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.tamano = tamano;
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
