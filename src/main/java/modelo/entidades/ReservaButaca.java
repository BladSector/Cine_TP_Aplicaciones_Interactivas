package modelo.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reserva_butaca",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_reserva_funcion_butaca",
                columnNames = {"funcion_id", "butaca_id"}
        )
)
public class ReservaButaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "funcion_id", nullable = false)
    private Funcion funcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "butaca_id", nullable = false)
    private Butaca butaca;

    @ManyToOne(optional = false)
    @JoinColumn(name = "espectador_id", nullable = false)
    private Espectador espectador;

    private LocalDateTime venceEn;

    protected ReservaButaca() {
    }

    public ReservaButaca(Funcion funcion, Butaca butaca, Espectador espectador, LocalDateTime venceEn) {
        this.funcion = funcion;
        this.butaca = butaca;
        this.espectador = espectador;
        this.venceEn = venceEn;
    }

    public void renovar(LocalDateTime nuevoVencimiento) {
        this.venceEn = nuevoVencimiento;
    }

    public boolean estaVencida(LocalDateTime momento) {
        return venceEn == null || !venceEn.isAfter(momento);
    }

    public long getId() {
        return id;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public Butaca getButaca() {
        return butaca;
    }

    public Espectador getEspectador() {
        return espectador;
    }

    public LocalDateTime getVenceEn() {
        return venceEn;
    }
}
