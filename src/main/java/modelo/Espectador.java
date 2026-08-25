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
    private static final int CANTIDAD_ENTRADAS_CLIENTE_FRECUENTE = 5;

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
        if (metodoDePago == null) {
            throw new IllegalArgumentException("El método de pago no puede ser null.");
        }

        if (!metodosDePago.contains(metodoDePago)) {
            metodosDePago.add(metodoDePago);
        }

        metodoDePago.asignarEspectador(this);
    }

    public boolean esClienteFrecuente() {
        return entradas.size() >= CANTIDAD_ENTRADAS_CLIENTE_FRECUENTE;
    }

    public int getCantidadEntradas() {
        return entradas.size();
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
        return metodosDePago.isEmpty() ? null : metodosDePago.get(0);
    }

    public List<MetodoDePago> getMetodosDePago() {
        return metodosDePago;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }
}
