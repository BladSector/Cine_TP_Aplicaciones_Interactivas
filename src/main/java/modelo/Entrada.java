package modelo;

import java.time.LocalDateTime;

public class Entrada {
    private int id;
    private double precio;
    private Funcion funcion;
    private Butaca butaca;
    private LocalDateTime horario;
    private EstadoEntrada estado;

    public Entrada(double precio, Funcion funcion, Butaca butaca, LocalDateTime horario) {
        this.id = 0;
        this.precio = precio;
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

        butaca.liberarButaca();
        this.estado = EstadoEntrada.REEMBOLSADA;
    }

    public void cancelar() {
        if (estado == EstadoEntrada.PAGADA || estado == EstadoEntrada.ESCANEADA) {
            throw new IllegalArgumentException("No se puede cancelar una entrada pagada o escaneada.");
        }

        butaca.liberarButaca();
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

    public int getId() {
        return id;
    }

    public double getPrecio() {
        return precio;
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
