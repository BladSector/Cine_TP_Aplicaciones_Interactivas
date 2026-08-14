import java.util.ArrayList;
import java.util.List;

public class Sala {
    private String nombre;
    private int capacidad;
    private List<Butaca> butacas;

    public Sala(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.butacas = new ArrayList<>();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la sala no son validos.");
        }
    }

    private boolean validarDatos() {
        return nombre != null
                && capacidad > 0;
    }

    public void agregarButaca(Butaca butaca) {
        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede estar vacía.");
        }

        if (butacas.size() >= capacidad) {
            throw new IllegalArgumentException("La sala ya alcanzo su capacidad máxima.");
        }

        butacas.add(butaca);
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
}