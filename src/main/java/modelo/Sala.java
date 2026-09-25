package modelo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sala")
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private int capacidad;
    @Enumerated(EnumType.STRING)
    private EstadoSala estado;
    @OneToMany(mappedBy = "sala")
    private List<Butaca> butacas;

    protected Sala(){
        this.butacas = new ArrayList<>();
        this.estado = EstadoSala.DISPONIBLE;
    }

    public Sala(String nombre, int capacidad) {
        this.id = 0;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.butacas = new ArrayList<>();
        this.estado = EstadoSala.DISPONIBLE;
    }

    public void actualizarDatos(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
    }

    public void agregarButaca(Butaca butaca) {
        butacas.add(butaca);
    }

    public void actualizarEstado(EstadoSala estado) {
        this.estado = estado;
    }

    public List<Butaca> getButacasDisponibles() {
        List<Butaca> disponibles = new ArrayList<>();

        for (Butaca butaca : butacas) {
            if (butaca.estaDisponible()) {
                disponibles.add(butaca);
            }
        }

        return disponibles;
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

    public int getCapacidad() {
        return capacidad;
    }

    public EstadoSala getEstado() {
        return estado == null ? EstadoSala.DISPONIBLE : estado;
    }
}
