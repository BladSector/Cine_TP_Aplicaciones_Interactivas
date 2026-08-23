package modelo;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "butaca")
public class Butaca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fila;
    private int numero;
    @Enumerated(EnumType.STRING)//El atributo estado que se define con un enum el MySQL lo guarda como String
    private EstadoButaca estado;
    private LocalDateTime bloqueoHasta;
    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;

    protected Butaca(){
    }

    public Butaca(String fila, int numero, Sala sala) {
        this.id = 0;
        this.fila = fila;
        this.numero = numero;
        this.estado = EstadoButaca.DISPONIBLE;
        this.bloqueoHasta = null;
        this.sala = sala;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la butaca no son validos.");
        }
    }

    private boolean validarDatos() {
        return fila != null && !fila.isBlank()
                && numero > 0
                && sala != null;
    }

    public boolean estaDisponible() {
        liberarBloqueoSiVencio();
        return estado == EstadoButaca.DISPONIBLE;
    }

    public void bloquear(int minutos) {
        if (minutos <= 0) {
            throw new IllegalArgumentException("Los minutos de bloqueo deben ser mayores a 0.");
        }

        if (!estaDisponible()) {
            throw new IllegalArgumentException("La butaca no esta disponible para bloquear.");
        }

        this.estado = EstadoButaca.BLOQUEADA;
        this.bloqueoHasta = LocalDateTime.now().plusMinutes(minutos);
    }

    public void liberarBloqueoSiVencio() {
        if (estado == EstadoButaca.BLOQUEADA
                && bloqueoHasta != null
                && LocalDateTime.now().isAfter(bloqueoHasta)) {
            liberarButaca();
        }
    }

    public void ocupar() {
        liberarBloqueoSiVencio();

        if (estado == EstadoButaca.OCUPADA) {
            throw new IllegalArgumentException("La butaca ya esta ocupada.");
        }

        if (estado == EstadoButaca.FUERA_DE_SERVICIO) {
            throw new IllegalArgumentException("La butaca esta fuera de servicio.");
        }

        this.estado = EstadoButaca.OCUPADA;
        this.bloqueoHasta = null;
    }

    public void liberarButaca() {
        this.estado = EstadoButaca.DISPONIBLE;
        this.bloqueoHasta = null;
    }

    public void marcarFueraDeServicio() {
        this.estado = EstadoButaca.FUERA_DE_SERVICIO;
        this.bloqueoHasta = null;
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

    public String getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public boolean isOcupada() {
        return estado == EstadoButaca.OCUPADA;
    }

    public EstadoButaca getEstado() {
        liberarBloqueoSiVencio();
        return estado;
    }

    public LocalDateTime getBloqueoHasta() {
        return bloqueoHasta;
    }

    public Sala getSala() {
        return sala;
    }
}
