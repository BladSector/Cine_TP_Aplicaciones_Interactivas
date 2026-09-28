package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario"),
        @Index(name = "idx_auditoria_accion", columnList = "accion")
})
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "empleado_id")
    private Integer empleadoId;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "tipo_actor", nullable = false, length = 30)
    private String tipoActor;

    @Column(nullable = false, length = 100)
    private String accion;

    @Column(length = 60)
    private String entidad;

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    protected Auditoria() {
    }

    public Auditoria(Integer empleadoId, String usuario, String tipoActor, String accion,
                     String entidad, Integer entidadId, String detalle, String resultado) {
        this.empleadoId = empleadoId;
        this.usuario = usuario;
        this.tipoActor = tipoActor;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalle = detalle;
        this.resultado = resultado;
        this.fechaHora = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public Integer getEmpleadoId() {
        return empleadoId;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getTipoActor() {
        return tipoActor;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Integer getEntidadId() {
        return entidadId;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
