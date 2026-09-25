package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "empleado")
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String apellido;
    @Column(unique = true, nullable = false)
    private String usuario;
    @Column(nullable = false)
    private String contrasenia;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private RolEmpleado rol;
    private boolean activo;

    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String usuario, String contrasenia, RolEmpleado rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.activo = true;
    }

    public void actualizarDatos(String nombre, String apellido, String usuario, RolEmpleado rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.rol = rol;
    }

    public void cambiarContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
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

    public String getUsuario() {
        return usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public RolEmpleado getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
