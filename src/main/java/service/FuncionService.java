package service;

import modelo.FormatoFuncion;
import modelo.Funcion;
import modelo.Pelicula;
import modelo.Sala;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.FuncionRepository;
import repository.PeliculaRepository;
import repository.SalaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class FuncionService {
    private final FuncionRepository funcionRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;

    public FuncionService(FuncionRepository funcionRepository, PeliculaRepository peliculaRepository, SalaRepository salaRepository) {
        this.funcionRepository = funcionRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
    }

    public List<Funcion> listar() {
        return funcionRepository.findAll();
    }

    public Funcion buscarPorId(int id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una funcion con ese id."));
    }

    public Funcion guardar(LocalDate fecha, LocalTime horario, int peliculaId, int salaId, FormatoFuncion formato, double precioEntrada) {
        try {
            Pelicula pelicula = buscarPelicula(peliculaId);
            Sala sala = buscarSala(salaId);
            return funcionRepository.save(new Funcion(fecha, horario, pelicula, sala, formato, precioEntrada));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Funcion actualizar(int id, LocalDate fecha, LocalTime horario, int peliculaId, int salaId, FormatoFuncion formato, double precioEntrada) {
        try {
            Funcion funcion = buscarPorId(id);
            Pelicula pelicula = buscarPelicula(peliculaId);
            Sala sala = buscarSala(salaId);
            funcion.actualizarDatos(fecha, horario, pelicula, sala, formato, precioEntrada);
            return funcionRepository.save(funcion);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        Funcion funcion = buscarPorId(id);
        funcionRepository.delete(funcion);
    }

    private Pelicula buscarPelicula(int id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una pelicula con ese id."));
    }

    private Sala buscarSala(int id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una sala con ese id."));
    }
}
