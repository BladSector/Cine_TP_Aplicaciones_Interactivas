package service;

import modelo.Butaca;
import modelo.EstadoSala;
import modelo.Sala;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.SalaRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SalaService {
    private final SalaRepository salaRepository;
    private final ButacaRepository butacaRepository;

    public SalaService(SalaRepository salaRepository, ButacaRepository butacaRepository) {
        this.salaRepository = salaRepository;
        this.butacaRepository = butacaRepository;
    }

    public List<Sala> listar() {
        return salaRepository.findAll();
    }

    public Sala buscarPorId(int id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una sala con ese id."));
    }

    public Sala guardar(String nombre, int capacidad) {
        validarSala(nombre, capacidad);
        return salaRepository.save(new Sala(nombre.trim(), capacidad));
    }

    @Transactional
    public Sala guardarConButacas(String nombre, int filas, int butacasPorFila) {
        validarNombre(nombre);
        if (filas <= 0 || filas > 26 || butacasPorFila <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filas y butacas por fila deben ser mayores a 0.");
        }

        Sala sala = salaRepository.save(new Sala(nombre.trim(), filas * butacasPorFila));

        for (int fila = 0; fila < filas; fila++) {
            String letraFila = String.valueOf((char) ('A' + fila));

            for (int numero = 1; numero <= butacasPorFila; numero++) {
                Butaca butaca = new Butaca(letraFila, numero, sala);
                sala.agregarButaca(butaca);
                butacaRepository.save(butaca);
            }
        }

        return sala;
    }

    @Transactional
    public Sala guardarConMatriz(String nombre, List<UbicacionButaca> ubicaciones) {
        validarNombre(nombre);
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala debe tener al menos una butaca.");
        }
        validarUbicaciones(ubicaciones);

        Sala sala = salaRepository.save(new Sala(nombre.trim(), ubicaciones.size()));

        for (UbicacionButaca ubicacion : ubicaciones) {
            Butaca butaca = new Butaca(ubicacion.fila().trim().toUpperCase(), ubicacion.numero(), sala);
            sala.agregarButaca(butaca);
            butacaRepository.save(butaca);
        }

        return sala;
    }

    public Sala actualizar(int id, String nombre, int capacidad) {
        validarSala(nombre, capacidad);
        Sala sala = buscarPorId(id);
        int cantidadButacas = butacaRepository.findBySalaId(id).size();
        if (capacidad < cantidadButacas) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacidad no puede ser menor a la cantidad de butacas existentes.");
        }
        sala.actualizarDatos(nombre.trim(), capacidad);
        return salaRepository.save(sala);
    }

    public Sala cambiarEstado(int id, EstadoSala estado) {
        if (estado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado de la sala es obligatorio.");
        }

        Sala sala = buscarPorId(id);
        sala.actualizarEstado(estado);
        return salaRepository.save(sala);
    }

    @Transactional
    public Sala actualizarConMatriz(int id, String nombre, List<Integer> butacasActivasIds) {
        validarNombre(nombre);
        if (butacasActivasIds == null || butacasActivasIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala debe conservar al menos una butaca.");
        }

        try {
            Sala sala = buscarPorId(id);
            List<Butaca> butacasActuales = butacaRepository.findBySalaId(id);
            Set<Integer> idsActivos = butacasActivasIds.stream().collect(Collectors.toSet());
            boolean idsInvalidos = idsActivos.stream()
                    .anyMatch(butacaId -> butacasActuales.stream().noneMatch(butaca -> butaca.getId() == butacaId));

            if (idsInvalidos) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay butacas que no pertenecen a la sala.");
            }

            List<Butaca> butacasAEliminar = butacasActuales.stream()
                    .filter(butaca -> !idsActivos.contains(butaca.getId()))
                    .toList();

            butacaRepository.deleteAll(butacasAEliminar);
            butacaRepository.flush();
            sala.actualizarDatos(nombre.trim(), idsActivos.size());
            return salaRepository.save(sala);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede quitar una butaca que ya tiene entradas asociadas."
            );
        }
    }

    @Transactional
    public Sala actualizarDistribucion(int id, String nombre, List<UbicacionButaca> ubicaciones) {
        validarNombre(nombre);
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala debe conservar al menos una butaca.");
        }
        validarUbicaciones(ubicaciones);

        try {
            Sala sala = buscarPorId(id);
            List<Butaca> actuales = butacaRepository.findBySalaId(id);
            Map<String, Butaca> actualesPorPosicion = actuales.stream().collect(Collectors.toMap(
                    butaca -> posicion(butaca.getFila(), butaca.getNumero()),
                    Function.identity()
            ));
            Set<String> posicionesNuevas = ubicaciones.stream()
                    .map(ubicacion -> posicion(ubicacion.fila(), ubicacion.numero()))
                    .collect(Collectors.toSet());

            List<Butaca> aEliminar = actuales.stream()
                    .filter(butaca -> !posicionesNuevas.contains(posicion(butaca.getFila(), butaca.getNumero())))
                    .toList();
            butacaRepository.deleteAll(aEliminar);
            butacaRepository.flush();

            for (UbicacionButaca ubicacion : ubicaciones) {
                String posicion = posicion(ubicacion.fila(), ubicacion.numero());
                if (!actualesPorPosicion.containsKey(posicion)) {
                    Butaca nueva = new Butaca(ubicacion.fila().trim().toUpperCase(), ubicacion.numero(), sala);
                    sala.agregarButaca(nueva);
                    butacaRepository.save(nueva);
                }
            }

            sala.actualizarDatos(nombre.trim(), ubicaciones.size());
            return salaRepository.save(sala);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede quitar una butaca que ya tiene entradas asociadas."
            );
        }
    }

    public void eliminar(int id) {
        Sala sala = buscarPorId(id);
        try {
            salaRepository.delete(sala);
            salaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar una sala con funciones o entradas asociadas.");
        }
    }

    private void validarSala(String nombre, int capacidad) {
        validarNombre(nombre);
        if (capacidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La capacidad debe ser mayor a 0.");
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la sala es obligatorio.");
        }
    }

    private void validarUbicaciones(List<UbicacionButaca> ubicaciones) {
        Set<String> posiciones = ubicaciones.stream()
                .map(ubicacion -> {
                    if (ubicacion == null || ubicacion.fila() == null || ubicacion.fila().isBlank() || ubicacion.numero() <= 0) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cada butaca debe tener fila y numero validos.");
                    }
                    return ubicacion.fila().trim().toUpperCase() + "-" + ubicacion.numero();
                })
                .collect(Collectors.toSet());

        if (posiciones.size() != ubicaciones.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La matriz contiene butacas repetidas.");
        }
    }

    private String posicion(String fila, int numero) {
        return fila.trim().toUpperCase() + "-" + numero;
    }

    public record UbicacionButaca(String fila, int numero) {
    }
}
