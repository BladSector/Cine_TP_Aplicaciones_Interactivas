package modelo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="espectador")
public class Espectador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasenia;
    @OneToMany(mappedBy = "espectador")
    private List<Entrada> entradas;
    @OneToMany(mappedBy = "espectador")
    private List<MetodoDePago> metodosDePago;
    private boolean emailVerificado;
    private String tokenVerificacionEmail;
    private String tokenRecuperacionContrasenia;

    protected Espectador(){
        this.entradas= new ArrayList<>();
        this.metodosDePago = new ArrayList<>();
    }

    public Espectador(String nombre, String apellido,
                      String email, String contrasenia) {

        this.id = 0;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
        this.entradas = new ArrayList<>();
        this.metodosDePago = new ArrayList<>();
        this.emailVerificado = false;
        this.tokenVerificacionEmail = null;
        this.tokenRecuperacionContrasenia = null;
    }

    public void actualizarDatos(String nombre, String apellido, String email, String contrasenia) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contrasenia = contrasenia;
    }

    public void agregarEntrada(Entrada entrada){
        entradas.add(entrada);
    }

    public void verificarMail(){
        this.emailVerificado = true;
    }

    public void agregarMetodoDePago(MetodoDePago metodoDePago) {
        if (!metodosDePago.contains(metodoDePago)) {
            metodosDePago.add(metodoDePago);
        }

        metodoDePago.asignarEspectador(this);
    }

    public int getCantidadEntradas() {
        return entradas.size();
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public void asignarTokenVerificacionEmail(String token) {
        this.tokenVerificacionEmail = token;
    }

    public void asignarTokenRecuperacionContrasenia(String token) {
        this.tokenRecuperacionContrasenia = token;
    }

    public String getTokenVerificacionEmail() {
        return tokenVerificacionEmail;
    }

    public String getTokenRecuperacionContrasenia() {
        return tokenRecuperacionContrasenia;
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
        for (MetodoDePago metodoDePago : metodosDePago) {
            if (metodoDePago.isActiva()) {
                return metodoDePago;
            }
        }

        return null;
    }

    public List<MetodoDePago> getMetodosDePago() {
        return metodosDePago;
    }

    public List<MetodoDePago> getMetodosDePagoActivos() {
        return metodosDePago.stream()
                .filter(MetodoDePago::isActiva)
                .toList();
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }
}
