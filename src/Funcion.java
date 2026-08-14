import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Funcion {
    private LocalDate fecha;
    private LocalTime horario;
    private Pelicula pelicula;
    private Sala sala;
    private List<Entrada> entradas;

    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala) {
        this.fecha = fecha;
        this.horario = horario;
        this.pelicula = pelicula;
        this.sala = sala;
        this.entradas = new ArrayList<>();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la función no son válidos.");
        }
    }

    private boolean validarDatos() {
        return fecha != null
                && horario != null
                && pelicula != null
                && sala != null;
    }

    public Entrada venderEntrada(double precio, Butaca butaca) {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede estar vacía.");
        }

        if (!butaca.estaDisponible()) {
            throw new IllegalArgumentException("La butaca no está disponible.");
        }

        LocalDateTime horarioEntrada = LocalDateTime.of(fecha, horario);
        //this significa "esta misma función"
        Entrada entrada = new Entrada(precio, this, butaca, horarioEntrada);
        entrada.asignarButaca();

        entradas.add(entrada);

        return entrada;
    }
}