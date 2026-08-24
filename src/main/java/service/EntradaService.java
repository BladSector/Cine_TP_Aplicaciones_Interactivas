package service;

import modelo.Butaca;
import modelo.Entrada;
import modelo.Espectador;
import modelo.Funcion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.EntradaRepository;
import repository.EspectadorRepository;
import repository.FuncionRepository;

import java.util.List;

@Service
public class EntradaService {
    private final EntradaRepository entradaRepository;
    private final EspectadorRepository espectadorRepository;
    private final FuncionRepository funcionRepository;
    private final ButacaRepository butacaRepository;

    public EntradaService(EntradaRepository entradaRepository,
                          EspectadorRepository espectadorRepository,
                          FuncionRepository funcionRepository,
                          ButacaRepository butacaRepository) {
        this.entradaRepository = entradaRepository;
        this.espectadorRepository = espectadorRepository;
        this.funcionRepository = funcionRepository;
        this.butacaRepository = butacaRepository;
    }

    public List<Entrada> listar() {
        return entradaRepository.findAll();
    }

    public Entrada buscarPorId(int id) {
        return entradaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una entrada con ese id."));
    }

    public Entrada vender(double precio, int espectadorId, int funcionId, int butacaId) {
        try {
            Espectador espectador = buscarEspectador(espectadorId);
            Funcion funcion = buscarFuncion(funcionId);
            Butaca butaca = buscarButaca(butacaId);
            Entrada entrada = funcion.venderEntrada(precio, espectador, butaca);
            butacaRepository.save(butaca);
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Entrada marcarPendienteDePago(int id) {
        try {
            Entrada entrada = buscarPorId(id);
            entrada.marcarPendienteDePago();
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Entrada pagar(int id) {
        try {
            Entrada entrada = buscarPorId(id);
            entrada.pagar();
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Entrada escanear(int id) {
        try {
            Entrada entrada = buscarPorId(id);
            entrada.escanear();
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Entrada reembolsar(int id) {
        try {
            Entrada entrada = buscarPorId(id);
            entrada.reembolsarEntrada();
            butacaRepository.save(entrada.getButaca());
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Entrada cancelar(int id) {
        try {
            Entrada entrada = buscarPorId(id);
            entrada.cancelar();
            butacaRepository.save(entrada.getButaca());
            return entradaRepository.save(entrada);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        Entrada entrada = buscarPorId(id);
        entradaRepository.delete(entrada);
    }

    private Espectador buscarEspectador(int id) {
        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
    }

    private Funcion buscarFuncion(int id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una funcion con ese id."));
    }

    private Butaca buscarButaca(int id) {
        return butacaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una butaca con ese id."));
    }
}
