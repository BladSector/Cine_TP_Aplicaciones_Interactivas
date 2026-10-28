package modelo.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "suscripcion")
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "espectador_id", nullable = false)
    private Espectador espectador;
    @ManyToOne(optional = false)
    @JoinColumn(name = "membresia_id", nullable = false)
    private Membresia membresia;
    @ManyToOne(optional = false)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoDePago metodoDePago;
    @ManyToOne
    @JoinColumn(name = "membresia_programada_id")
    private Membresia membresiaProgramada;
    @ManyToOne
    @JoinColumn(name = "metodo_pago_programado_id")
    private MetodoDePago metodoPagoProgramado;
    @Column(name = "precio_cobrado")
    private double precioCobrado;
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;
    private boolean activa;

    protected Suscripcion() {
    }

    public Suscripcion(Espectador espectador, Membresia membresia, MetodoDePago metodoDePago,
                       double precioCobrado, LocalDate fechaInicio, LocalDate fechaFin) {
        this.espectador = espectador;
        this.membresia = membresia;
        this.metodoDePago = metodoDePago;
        this.precioCobrado = precioCobrado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = true;
    }

    public void desactivar() {
        this.activa = false;
    }

    public void programarCambio(Membresia membresia, MetodoDePago metodoDePago) {
        this.membresiaProgramada = membresia;
        this.metodoPagoProgramado = metodoDePago;
    }

    public boolean tieneCambioProgramado() {
        return membresiaProgramada != null;
    }

    public int getId() {
        return id;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public Membresia getMembresia() {
        return membresia;
    }

    public MetodoDePago getMetodoDePago() {
        return metodoDePago;
    }

    public Membresia getMembresiaProgramada() {
        return membresiaProgramada;
    }

    public MetodoDePago getMetodoPagoProgramado() {
        return metodoPagoProgramado;
    }

    public double getPrecioCobrado() {
        return precioCobrado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean isActiva() {
        return activa;
    }
}
