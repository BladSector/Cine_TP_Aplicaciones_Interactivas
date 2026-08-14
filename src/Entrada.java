import java.time.LocalDateTime;

public class Entrada {
    private double precio;
    private Funcion funcion;
    private Butaca butaca;
    private LocalDateTime horario;
    private boolean reembolsada;

    public Entrada(double precio, Funcion funcion, Butaca butaca, LocalDateTime horario) {
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
}
