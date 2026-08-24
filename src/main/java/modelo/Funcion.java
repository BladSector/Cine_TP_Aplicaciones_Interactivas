package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table (name = "funcion")
public class Funcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private LocalDate fecha;
    private LocalTime horario;
    @ManyToOne
    @JoinColumn(name = "pelicula_id")
    private Pelicula pelicula;
    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;
    @Enumerated(EnumType.STRING)
    private FormatoFuncion formato;
    @OneToMany(mappedBy = "funcion")
    private List<Entrada> entradas;

    protected Funcion(){
        this.entradas= new ArrayList<>();
    }
    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala) {
        this(fecha, horario, pelicula, sala, FormatoFuncion.DOS_D);
    }

    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato) {
        this.id = 0;
        this.fecha = fecha;
        this.horario = horario;
        this.pelicula = pelicula;
        this.sala = sala;
        this.formato = formato;
        this.entradas = new ArrayList<>();

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la función no son válidos.");
        }
    }

    private boolean validarDatos() {
        return fecha != null
                && horario != null
                && pelicula != null
                && sala != null
                && formato != null;
    }

    public void actualizarDatos(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato) {
        this.fecha = fecha;
        this.horario = horario;
        this.pelicula = pelicula;
        this.sala = sala;
        this.formato = formato;

        if (!validarDatos()) {
            throw new IllegalArgumentException("Los datos de la función no son válidos.");
        }
    }

    public Entrada venderEntrada(double precio, Espectador espectador, Butaca butaca) {
        if (espectador == null) {
            throw new IllegalArgumentException("El espectador no puede estar vacio.");
        }

        if (butaca == null) {
            throw new IllegalArgumentException("La butaca no puede estar vacía.");
        }

        if (!butaca.estaDisponible()) {
            throw new IllegalArgumentException("La butaca no está disponible.");
        }

        LocalDateTime horarioEntrada = LocalDateTime.of(fecha, horario);
        //this significa "esta misma función"
        Entrada entrada = new Entrada(precio, espectador, this, butaca, horarioEntrada);
        entrada.asignarButaca();

        entradas.add(entrada);
        espectador.agregarEntrada(entrada);

        return entrada;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public Sala getSala() {
        return sala;
    }

    public FormatoFuncion getFormato() {
        return formato;
    }
}
