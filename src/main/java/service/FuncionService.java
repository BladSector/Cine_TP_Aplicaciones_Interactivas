package service;

import modelo.FormatoFuncion;
import modelo.Funcion;
import modelo.IdiomaFuncion;
import modelo.Pelicula;
import modelo.Sala;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.FuncionRepository;
import repository.PeliculaRepository;
import repository.SalaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public Funcion guardar(LocalDate fecha, LocalTime horario, int peliculaId, int salaId, FormatoFuncion formato, IdiomaFuncion idioma, double precioEntrada) {
        validarDatos(fecha, horario, formato, idioma, precioEntrada);
        Pelicula pelicula = buscarPelicula(peliculaId);
        Sala sala = buscarSala(salaId);
        validarDisponibilidadSala(fecha, horario, pelicula, sala, null);
        return funcionRepository.save(new Funcion(fecha, horario, pelicula, sala, formato, idioma, precioEntrada));
    }

    public Funcion actualizar(int id, LocalDate fecha, LocalTime horario, int peliculaId, int salaId, FormatoFuncion formato, IdiomaFuncion idioma, double precioEntrada) {
        validarDatos(fecha, horario, formato, idioma, precioEntrada);
        Funcion funcion = buscarPorId(id);
        Pelicula pelicula = buscarPelicula(peliculaId);
        Sala sala = buscarSala(salaId);
        validarDisponibilidadSala(fecha, horario, pelicula, sala, id);
        funcion.actualizarDatos(fecha, horario, pelicula, sala, formato, idioma, precioEntrada);
        return funcionRepository.save(funcion);
    }

    public Funcion actualizarHorario(int id, LocalDate fecha, LocalTime horario) {
        Funcion funcion = buscarPorId(id);
        validarDatos(fecha, horario, funcion.getFormato(), funcion.getIdioma(), funcion.getPrecioEntrada());
        validarDisponibilidadSala(fecha, horario, funcion.getPelicula(), funcion.getSala(), id);
        funcion.actualizarDatos(
                fecha,
                horario,
                funcion.getPelicula(),
                funcion.getSala(),
                funcion.getFormato(),
                funcion.getIdioma(),
                funcion.getPrecioEntrada()
        );
        return funcionRepository.save(funcion);
    }

    public void eliminar(int id) {
        Funcion funcion = buscarPorId(id);
        try {
            funcionRepository.delete(funcion);
            funcionRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar una funcion con entradas asociadas.");
        }
    }

    private Pelicula buscarPelicula(int id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una pelicula con ese id."));
    }

    private Sala buscarSala(int id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una sala con ese id."));
    }

    private void validarDisponibilidadSala(LocalDate fecha, LocalTime horario, Pelicula pelicula, Sala sala, Integer funcionIdIgnorada) {
        LocalDateTime inicioNuevaFuncion = LocalDateTime.of(fecha, horario);
        LocalDateTime finNuevaFuncion = inicioNuevaFuncion.plusMinutes(pelicula.getDuracion());

        List<Funcion> funcionesDeLaSala = funcionRepository.findBySalaIdAndFecha(sala.getId(), fecha);

        for (Funcion funcionExistente : funcionesDeLaSala) {
            if (funcionIdIgnorada != null && funcionExistente.getId() == funcionIdIgnorada) {
                continue;
            }

            LocalDateTime inicioFuncionExistente = LocalDateTime.of(funcionExistente.getFecha(), funcionExistente.getHorario());
            LocalDateTime finFuncionExistente = inicioFuncionExistente.plusMinutes(funcionExistente.getPelicula().getDuracion());

            boolean horariosSuperpuestos = inicioNuevaFuncion.isBefore(finFuncionExistente)
                    && inicioFuncionExistente.isBefore(finNuevaFuncion);

            if (horariosSuperpuestos) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La sala ya tiene una funcion cargada en ese rango horario."
                );
            }
        }
    }

    private void validarDatos(LocalDate fecha, LocalTime horario, FormatoFuncion formato,
                              IdiomaFuncion idioma, double precioEntrada) {
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de la funcion debe ser de hoy o posterior.");
        }
        if (horario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El horario de la funcion es obligatorio.");
        }
        if (formato == null || idioma == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El formato y el idioma son obligatorios.");
        }
        if (precioEntrada <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de la entrada debe ser mayor a 0.");
        }
    }
}
