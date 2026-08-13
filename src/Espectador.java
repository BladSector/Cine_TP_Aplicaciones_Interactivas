import java.util.ArrayList;
import java.util.List;

public class Espectador {
    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;
    private List<Entrada> entradas;
    private MetodoDePago metodoDePago;
    private boolean emailVerificado;

    public Espectador(String nombre, String apellido,
                      String email, String contrasenia) {

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
}
