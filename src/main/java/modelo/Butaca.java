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
    }

    public void actualizarDatos(String fila, int numero, Sala sala) {
        this.fila = fila;
        this.numero = numero;
        this.sala = sala;
    }

    public boolean estaDisponible() {
        return estado == EstadoButaca.DISPONIBLE;
    }

    public void bloquear(int minutos) {
        this.estado = EstadoButaca.BLOQUEADA;
        this.bloqueoHasta = LocalDateTime.now().plusMinutes(minutos);
    }

    public void ocupar() {
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
        return estado;
    }

    public LocalDateTime getBloqueoHasta() {
        return bloqueoHasta;
    }

    public Sala getSala() {
        return sala;
    }
}
