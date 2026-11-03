package desktop.duenio.dialogos;

public record Opcion(int id, String nombre) {
    @Override
    public String toString() {
        return nombre;
    }
}
