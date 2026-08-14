public class Butaca {
    private String fila;
    private int numero;
    private boolean ocupada;

    public Butaca(String fila, int numero) {
        this.fila = fila;
        this.numero = numero;
        this.ocupada = false;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la butaca no son validos.");
        }
    }

    private boolean validarDatos() {
        return fila != null && !fila.isBlank()
                && numero > 0;
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
}
