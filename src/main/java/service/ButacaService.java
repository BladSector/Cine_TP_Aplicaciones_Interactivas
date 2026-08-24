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
        return butacaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una butaca con ese id."));
    }

    public Butaca guardar(String fila, int numero, int salaId) {
        try {
            Sala sala = buscarSala(salaId);
            Butaca butaca = new Butaca(fila, numero, sala);
            sala.agregarButaca(butaca);
            return butacaRepository.save(butaca);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Butaca actualizar(int id, String fila, int numero, int salaId) {
        try {
            Butaca butaca = buscarPorId(id);
            Sala sala = buscarSala(salaId);
            butaca.actualizarDatos(fila, numero, sala);
            return butacaRepository.save(butaca);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Butaca bloquear(int id, int minutos) {
        try {
            Butaca butaca = buscarPorId(id);
            butaca.bloquear(minutos);
            return butacaRepository.save(butaca);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Butaca ocupar(int id) {
        try {
            Butaca butaca = buscarPorId(id);
            butaca.ocupar();
            return butacaRepository.save(butaca);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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
}
