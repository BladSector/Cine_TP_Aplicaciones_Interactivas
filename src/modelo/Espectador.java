package modelo;

import java.util.ArrayList;
import java.util.List;

public class Espectador {
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;
    private List<Entrada> entradas;
    private MetodoDePago metodoDePago;
    private boolean emailVerificado;

    public Espectador(String nombre, String apellido,
                      String email, String contrasenia) {

        this.id = 0;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
        this.entradas = new ArrayList<>();
        this.emailVerificado = false;
    }

    public void agregarEntrada(Entrada entrada){
        entradas.add(entrada);
    }

    public void verificarMail(){
        this.emailVerificado = true;
    }

    public void agregarMetodoDePago(MetodoDePago metodoDePago) {
        this.metodoDePago = metodoDePago;
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

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public MetodoDePago getMetodoDePago() {
        return metodoDePago;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }
}
