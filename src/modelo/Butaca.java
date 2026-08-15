package modelo;

public class Butaca {
    private int id;
    private String fila;
    private int numero;
    private boolean ocupada;
    private Sala sala;

    public Butaca(String fila, int numero, Sala sala) {
        this.id = 0;
        this.fila = fila;
        this.numero = numero;
        this.ocupada = false;
        this.sala = sala;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la butaca no son validos.");
        }
    }

    private boolean validarDatos() {
        return fila != null && !fila.isBlank()
                && numero > 0
                && sala != null;
    }
    //!ocupada devuelve lo contrario (si es false, o sea no está ocupada, devuelve true y viceversa)
    public boolean estaDisponible() {
        return !ocupada;
    }

    public void ocupar() {
        if (!estaDisponible()) {
            throw new IllegalArgumentException("La butaca ya esta ocupada.");
        }

        this.ocupada = true;
    }

    public void liberarButaca() {
        this.ocupada = false;
    }

    public void asignarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor a 0.");
        }

        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public Sala getSala() {
        return sala;
    }
}
