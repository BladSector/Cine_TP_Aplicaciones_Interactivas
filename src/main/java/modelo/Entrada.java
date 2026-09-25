package modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table (name = "entrada")
public class Entrada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double precio;
    @ManyToOne
    @JoinColumn(name = "espectador_id")
    private Espectador espectador;
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
    @ManyToOne
    @JoinColumn(name = "funcion_id")
    private Funcion funcion;
    @ManyToOne
    @JoinColumn(name = "butaca_id")
    private Butaca butaca;
    private LocalDateTime horario;
    @Enumerated(EnumType.STRING)
    private EstadoEntrada estado;

    protected Entrada(){
    }

    public Entrada(double precio, Espectador espectador, Funcion funcion, Butaca butaca, LocalDateTime horario) {
        this.id = 0;
        this.precio = precio;
        this.espectador = espectador;
        this.funcion = funcion;
        this.butaca = butaca;
        this.horario = horario;
        this.estado = EstadoEntrada.GENERADA;
    }

    public void marcarPendienteDePago() {
        this.estado = EstadoEntrada.PENDIENTE_DE_PAGO;
    }

    public void pagar() {
        this.estado = EstadoEntrada.PAGADA;
    }

    public void escanear() {
        this.estado = EstadoEntrada.ESCANEADA;
    }

    public void reembolsarEntrada() {
        this.estado = EstadoEntrada.REEMBOLSADA;
    }

    public void cancelar() {
        this.estado = EstadoEntrada.CANCELADA;
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public void asignarReembolsada(boolean reembolsada) {
        this.estado = reembolsada ? EstadoEntrada.REEMBOLSADA : EstadoEntrada.GENERADA;
    }

    public void asignarEstado(EstadoEntrada estado) {
        this.estado = estado;
    }

    public void asignarTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public int getId() {
        return id;
    }

    public double getPrecio() {
        return precio;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public Butaca getButaca() {
        return butaca;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public boolean isReembolsada() {
        return estado == EstadoEntrada.REEMBOLSADA;
    }

    public EstadoEntrada getEstado() {
        return estado;
    }
}
