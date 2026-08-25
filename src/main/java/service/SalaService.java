package service;

import modelo.Butaca;
import modelo.Sala;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.SalaRepository;

import java.util.List;

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
        try {
            return salaRepository.save(new Sala(nombre, capacidad));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Sala guardarConButacas(String nombre, int filas, int butacasPorFila) {
        if (filas <= 0 || butacasPorFila <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filas y butacas por fila deben ser mayores a 0.");
        }

        try {
            Sala sala = salaRepository.save(new Sala(nombre, filas * butacasPorFila));

            for (int fila = 0; fila < filas; fila++) {
                String letraFila = String.valueOf((char) ('A' + fila));

                for (int numero = 1; numero <= butacasPorFila; numero++) {
                    Butaca butaca = new Butaca(letraFila, numero, sala);
                    sala.agregarButaca(butaca);
                    butacaRepository.save(butaca);
                }
            }

            return sala;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Sala guardarConMatriz(String nombre, List<UbicacionButaca> ubicaciones) {
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala debe tener al menos una butaca.");
        }

        try {
            Sala sala = salaRepository.save(new Sala(nombre, ubicaciones.size()));

            for (UbicacionButaca ubicacion : ubicaciones) {
                Butaca butaca = new Butaca(ubicacion.fila(), ubicacion.numero(), sala);
                sala.agregarButaca(butaca);
                butacaRepository.save(butaca);
            }

            return sala;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Sala actualizar(int id, String nombre, int capacidad) {
        try {
            Sala sala = buscarPorId(id);
            sala.actualizarDatos(nombre, capacidad);
            return salaRepository.save(sala);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        Sala sala = buscarPorId(id);
        salaRepository.delete(sala);
    }

    public record UbicacionButaca(String fila, int numero) {
    }
}
