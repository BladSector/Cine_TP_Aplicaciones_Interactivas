package modelo;

import java.time.LocalDate;
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
    @Enumerated(EnumType.STRING)
    private IdiomaFuncion idioma;
    private double precioEntrada;
    @OneToMany(mappedBy = "funcion")
    private List<Entrada> entradas;

    protected Funcion(){
        this.entradas= new ArrayList<>();
    }
    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala) {
        this(fecha, horario, pelicula, sala, FormatoFuncion.DOS_D, IdiomaFuncion.SUBTITULADA, 0);
    }

    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato) {
        this(fecha, horario, pelicula, sala, formato, IdiomaFuncion.SUBTITULADA, 0);
    }

    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato, double precioEntrada) {
        this(fecha, horario, pelicula, sala, formato, IdiomaFuncion.SUBTITULADA, precioEntrada);
    }

    public Funcion(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato, IdiomaFuncion idioma, double precioEntrada) {
        this.id = 0;
        this.fecha = fecha;
        this.horario = horario;
        this.pelicula = pelicula;
        this.sala = sala;
        this.formato = formato;
        this.idioma = idioma;
        this.precioEntrada = precioEntrada;
        this.entradas = new ArrayList<>();
    }

    public void actualizarDatos(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato) {
        actualizarDatos(fecha, horario, pelicula, sala, formato, idioma, precioEntrada);
    }

    public void actualizarDatos(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato, double precioEntrada) {
        actualizarDatos(fecha, horario, pelicula, sala, formato, idioma, precioEntrada);
    }

    public void actualizarDatos(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, FormatoFuncion formato, IdiomaFuncion idioma, double precioEntrada) {
        this.fecha = fecha;
        this.horario = horario;
        this.pelicula = pelicula;
        this.sala = sala;
        this.formato = formato;
        this.idioma = idioma;
        this.precioEntrada = precioEntrada;
    }

    public void asignarId(int id) {
        this.id = id;
    }

    public void agregarEntrada(Entrada entrada) {
        entradas.add(entrada);
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

    public IdiomaFuncion getIdioma() {
        return idioma;
    }

    public double getPrecioEntrada() {
        return precioEntrada;
    }
}
