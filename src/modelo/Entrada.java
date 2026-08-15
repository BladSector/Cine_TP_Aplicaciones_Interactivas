package modelo;

import java.time.LocalDateTime;

public class Entrada {
    private int id;
    private double precio;
    private Funcion funcion;
    private Butaca butaca;
    private LocalDateTime horario;
    private boolean reembolsada;

    public Entrada(double precio, Funcion funcion, Butaca butaca, LocalDateTime horario) {
        this.id = 0;
        this.precio = precio;
        this.funcion = funcion;
        this.butaca = butaca;
        this.horario = horario;
        this.reembolsada = false;

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

    public void reembolsarEntrada() {
        butaca.liberarButaca();
        this.reembolsada = true;
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public void asignarReembolsada(boolean reembolsada) {
        this.reembolsada = reembolsada;
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
        return reembolsada;
    }
}
