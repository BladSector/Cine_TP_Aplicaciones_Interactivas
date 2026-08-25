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

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la entrada no son validos.");
        }
    }

    private boolean validarDatos() {
        return precio > 0
                && espectador != null
                && funcion != null
                && butaca != null
                && horario != null;
    }

    public Butaca asignarButaca() {
        if (!butaca.estaDisponible()) {
            throw new IllegalArgumentException("La butaca no esta disponible.");
        }

        butaca.ocupar();
        return butaca;
    }

    public void marcarPendienteDePago() {
        if (estado != EstadoEntrada.GENERADA) {
            throw new IllegalArgumentException("Solo una entrada generada puede quedar pendiente de pago.");
        }

        this.estado = EstadoEntrada.PENDIENTE_DE_PAGO;
    }

    public void pagar() {
        if (estado != EstadoEntrada.GENERADA && estado != EstadoEntrada.PENDIENTE_DE_PAGO) {
            throw new IllegalArgumentException("La entrada no se puede pagar en su estado actual.");
        }

        this.estado = EstadoEntrada.PAGADA;
    }

    public void escanear() {
        if (estado != EstadoEntrada.PAGADA) {
            throw new IllegalArgumentException("Solo una entrada pagada puede escanearse.");
        }

        this.estado = EstadoEntrada.ESCANEADA;
    }

    public void reembolsarEntrada() {
        if (estado == EstadoEntrada.REEMBOLSADA) {
            throw new IllegalArgumentException("La entrada ya esta reembolsada.");
        }

        if (estado == EstadoEntrada.ESCANEADA) {
            throw new IllegalArgumentException("No se puede reembolsar una entrada ya escaneada.");
        }

        this.estado = EstadoEntrada.REEMBOLSADA;
    }

    public void cancelar() {
        if (estado == EstadoEntrada.PAGADA || estado == EstadoEntrada.ESCANEADA) {
            throw new IllegalArgumentException("No se puede cancelar una entrada pagada o escaneada.");
        }

        this.estado = EstadoEntrada.CANCELADA;
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public void asignarReembolsada(boolean reembolsada) {
        this.estado = reembolsada ? EstadoEntrada.REEMBOLSADA : EstadoEntrada.GENERADA;
    }

    public void asignarEstado(EstadoEntrada estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la entrada no puede ser null.");
        }

        this.estado = estado;
    }

    public void asignarTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("El ticket no puede ser null.");
        }

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
