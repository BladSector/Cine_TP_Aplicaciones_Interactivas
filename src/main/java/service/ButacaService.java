package service;

import modelo.Butaca;
import modelo.Sala;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.SalaRepository;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ButacaService {
    private final ButacaRepository butacaRepository;
    private final SalaRepository salaRepository;

    public ButacaService(ButacaRepository butacaRepository, SalaRepository salaRepository) {
        this.butacaRepository = butacaRepository;
        this.salaRepository = salaRepository;
    }

    public List<Butaca> listar() {
        return butacaRepository.findAll();
    }

    public Butaca buscarPorId(int id) {
        Butaca butaca = butacaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una butaca con ese id."));
        liberarBloqueoVencido(butaca);
        return butaca;
    }

    public Butaca guardar(String fila, int numero, int salaId) {
        validarDatos(fila, numero);
        Sala sala = buscarSala(salaId);
        if (butacaRepository.findBySalaId(salaId).size() >= sala.getCapacidad()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sala ya alcanzo su capacidad maxima.");
        }
        Butaca butaca = new Butaca(fila.trim().toUpperCase(), numero, sala);
        sala.agregarButaca(butaca);
        return butacaRepository.save(butaca);
    }

    public Butaca actualizar(int id, String fila, int numero, int salaId) {
        validarDatos(fila, numero);
        Butaca butaca = buscarPorId(id);
        Sala sala = buscarSala(salaId);
        butaca.actualizarDatos(fila.trim().toUpperCase(), numero, sala);
        return butacaRepository.save(butaca);
    }

    public Butaca bloquear(int id, int minutos) {
        if (minutos <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los minutos de bloqueo deben ser mayores a 0.");
        }
        Butaca butaca = buscarPorId(id);
        if (!butaca.estaDisponible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca no esta disponible para bloquear.");
        }
        butaca.bloquear(minutos);
        return butacaRepository.save(butaca);
    }

    public Butaca ocupar(int id) {
        Butaca butaca = buscarPorId(id);
        if (!butaca.estaDisponible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca no esta disponible.");
        }
        butaca.ocupar();
        return butacaRepository.save(butaca);
    }

    public Butaca liberar(int id) {
        Butaca butaca = buscarPorId(id);
        butaca.liberarButaca();
        return butacaRepository.save(butaca);
    }

    public Butaca marcarFueraDeServicio(int id) {
        Butaca butaca = buscarPorId(id);
        butaca.marcarFueraDeServicio();
        return butacaRepository.save(butaca);
    }

    public void eliminar(int id) {
        Butaca butaca = buscarPorId(id);
        butacaRepository.delete(butaca);
    }

    private Sala buscarSala(int id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una sala con ese id."));
    }

    private void liberarBloqueoVencido(Butaca butaca) {
        if (butaca.getEstado() == modelo.EstadoButaca.BLOQUEADA
                && butaca.getBloqueoHasta() != null
                && LocalDateTime.now().isAfter(butaca.getBloqueoHasta())) {
            butaca.liberarButaca();
            butacaRepository.save(butaca);
        }
    }

    private void validarDatos(String fila, int numero) {
        if (fila == null || fila.isBlank() || numero <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fila es obligatoria y el numero debe ser mayor a 0.");
        }
    }
}
