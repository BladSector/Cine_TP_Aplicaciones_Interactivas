package modelo.entidades;

import modelo.enums.TamanoProductoConfiteria;
import modelo.enums.TipoProductoConfiteria;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
    @Lob
    private String imagenUrl;

    protected ProductoConfiteria() {
    }

    public ProductoConfiteria(String nombre, double precio,
                              TipoProductoConfiteria tipo,
                              TamanoProductoConfiteria tamano,
                              String imagenUrl) {
        this.id = 0;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.tamano = tamano;
        this.imagenUrl = imagenUrl;
    }

    public void actualizarDatos(String nombre, double precio, TipoProductoConfiteria tipo,
                                TamanoProductoConfiteria tamano, String imagenUrl) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.tamano = tamano;
        this.imagenUrl = imagenUrl;
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

    public String getImagenUrl() {
        return imagenUrl;
    }
}
