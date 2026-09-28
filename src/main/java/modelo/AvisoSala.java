package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "aviso_sala", indexes = {
        @Index(name = "idx_aviso_destinatario_leido", columnList = "destinatario_empleado_id, leido"),
        @Index(name = "idx_aviso_fecha", columnList = "fecha_hora")
})
public class AvisoSala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoSala estado;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "creado_por_empleado_id")
    private Integer creadoPorEmpleadoId;

    @Column(name = "creado_por_usuario", nullable = false, length = 100)
    private String creadoPorUsuario;

    @Column(name = "destinatario_empleado_id", nullable = false)
    private int destinatarioEmpleadoId;

    @Column(name = "destinatario_usuario", nullable = false, length = 100)
    private String destinatarioUsuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private boolean leido;

    protected AvisoSala() {
    }

    public AvisoSala(Sala sala, EstadoSala estado, String descripcion,
                     Integer creadoPorEmpleadoId, String creadoPorUsuario,
                     int destinatarioEmpleadoId, String destinatarioUsuario) {
        this.sala = sala;
        this.estado = estado;
        this.descripcion = descripcion;
        this.creadoPorEmpleadoId = creadoPorEmpleadoId;
        this.creadoPorUsuario = creadoPorUsuario;
        this.destinatarioEmpleadoId = destinatarioEmpleadoId;
        this.destinatarioUsuario = destinatarioUsuario;
        this.fechaHora = LocalDateTime.now();
        this.leido = false;
    }

    public void marcarLeido() {
        this.leido = true;
    }

    public long getId() {
        return id;
    }

    public Sala getSala() {
        return sala;
    }

    public EstadoSala getEstado() {
        return estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getCreadoPorEmpleadoId() {
        return creadoPorEmpleadoId;
    }

    public String getCreadoPorUsuario() {
        return creadoPorUsuario;
    }

    public int getDestinatarioEmpleadoId() {
        return destinatarioEmpleadoId;
    }

    public String getDestinatarioUsuario() {
        return destinatarioUsuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public boolean isLeido() {
        return leido;
    }
}
