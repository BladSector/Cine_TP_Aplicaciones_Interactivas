package modelo.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "membresia")
public class Membresia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    @Column(name = "precio_mensual")
    private double precioMensual;
    private String descripcion;
    @Column(columnDefinition = "TEXT")
    private String beneficios;
    private boolean destacada;

    protected Membresia() {
    }

    public Membresia(String nombre, double precioMensual, String descripcion,
                     String beneficios, boolean destacada) {
        this.nombre = nombre;
        this.precioMensual = precioMensual;
        this.descripcion = descripcion;
        this.beneficios = beneficios;
        this.destacada = destacada;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioMensual() {
        return precioMensual;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public boolean isDestacada() {
        return destacada;
    }

    public List<String> listarBeneficios() {
        if (beneficios == null || beneficios.isBlank()) {
            return List.of();
        }
        return Arrays.stream(beneficios.split("\\R"))
                .map(String::trim)
                .filter(linea -> !linea.isEmpty())
                .toList();
    }
}
