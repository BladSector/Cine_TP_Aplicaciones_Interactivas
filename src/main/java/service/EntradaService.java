package service;

import modelo.Butaca;
import modelo.Entrada;
import modelo.Espectador;
import modelo.EstadoEntrada;
import modelo.EstadoSala;
import modelo.Funcion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.EntradaRepository;
import repository.EspectadorRepository;
import repository.FuncionRepository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaService {
    private final EntradaRepository entradaRepository;
    private final EspectadorRepository espectadorRepository;
    private final FuncionRepository funcionRepository;
    private final ButacaRepository butacaRepository;
    private final PrecioService precioService;

    public EntradaService(EntradaRepository entradaRepository,
                          EspectadorRepository espectadorRepository,
                          FuncionRepository funcionRepository,
                          ButacaRepository butacaRepository,
                          PrecioService precioService) {
        this.entradaRepository = entradaRepository;
        this.espectadorRepository = espectadorRepository;
        this.funcionRepository = funcionRepository;
        this.butacaRepository = butacaRepository;
        this.precioService = precioService;
    }

    public List<Entrada> listar() {
        return entradaRepository.findAll();
    }

    public List<Entrada> listarPorEspectador(int espectadorId) {
        return entradaRepository.findByEspectadorId(espectadorId);
    }

    public Entrada buscarPorId(int id) {
        return entradaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una entrada con ese id."));
    }

    @Transactional
    public Entrada vender(double precio, int espectadorId, int funcionId, int butacaId) {
        Espectador espectador = buscarEspectador(espectadorId);
        Funcion funcion = buscarFuncion(funcionId);
        Butaca butaca = buscarButaca(butacaId);
        double precioCalculado = precioService.calcularPrecioUnitario(funcion, espectador, funcion.getPrecioEntrada());
        return crearEntrada(funcion, espectador, butaca, precioCalculado);
    }

    public Entrada marcarPendienteDePago(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getEstado() != EstadoEntrada.GENERADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo una entrada generada puede quedar pendiente de pago.");
        }
        entrada.marcarPendienteDePago();
        return entradaRepository.save(entrada);
    }

    public Entrada pagar(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getEstado() != EstadoEntrada.GENERADA && entrada.getEstado() != EstadoEntrada.PENDIENTE_DE_PAGO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada no se puede pagar en su estado actual.");
        }
        entrada.pagar();
        return entradaRepository.save(entrada);
    }

    public Entrada escanear(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getEstado() != EstadoEntrada.PAGADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo una entrada pagada puede escanearse.");
        }
        entrada.escanear();
        return entradaRepository.save(entrada);
    }

    public Entrada reembolsar(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getEstado() == EstadoEntrada.REEMBOLSADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada ya esta reembolsada.");
        }
        if (entrada.getEstado() == EstadoEntrada.ESCANEADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reembolsar una entrada ya escaneada.");
        }
        entrada.reembolsarEntrada();
        return entradaRepository.save(entrada);
    }

    public Entrada cancelar(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getEstado() == EstadoEntrada.PAGADA || entrada.getEstado() == EstadoEntrada.ESCANEADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cancelar una entrada pagada o escaneada.");
        }
        entrada.cancelar();
        return entradaRepository.save(entrada);
    }

    public void eliminar(int id) {
        Entrada entrada = buscarPorId(id);
        if (entrada.getTicket() != null || entrada.getEstado() != EstadoEntrada.GENERADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar una entrada que forma parte de una compra.");
        }
        entradaRepository.delete(entrada);
    }

    public Entrada crearEntrada(Funcion funcion, Espectador espectador, Butaca butaca, double precio) {
        validarVenta(funcion, espectador, butaca, precio);
        Entrada entrada = new Entrada(
                precio,
                espectador,
                funcion,
                butaca,
                LocalDateTime.of(funcion.getFecha(), funcion.getHorario())
        );
        funcion.agregarEntrada(entrada);
        espectador.agregarEntrada(entrada);
        return entradaRepository.save(entrada);
    }

    public Entrada crearEntradaPagada(Funcion funcion, Espectador espectador, Butaca butaca, double precio) {
        Entrada entrada = crearEntrada(funcion, espectador, butaca, precio);
        entrada.pagar();
        return entradaRepository.save(entrada);
    }

    private void validarVenta(Funcion funcion, Espectador espectador, Butaca butaca, double precio) {
        if (!espectador.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe verificar el email antes de comprar.");
        }
        if (precio <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio de la entrada debe ser mayor a 0.");
        }
        if (funcion.getSala().getEstado() != EstadoSala.DISPONIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La sala no esta disponible para realizar compras."
            );
        }
        LocalDateTime inicioFuncion = LocalDateTime.of(funcion.getFecha(), funcion.getHorario());
        if (inicioFuncion.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden comprar entradas para una funcion pasada.");
        }
        if (butaca.getSala() == null || butaca.getSala().getId() != funcion.getSala().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca no pertenece a la sala de la funcion.");
        }
        if (!butaca.estaDisponible()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca no esta habilitada.");
        }
        if (entradaRepository.existeEntradaActivaParaButaca(
                funcion.getId(), butaca.getId(), List.of(EstadoEntrada.REEMBOLSADA, EstadoEntrada.CANCELADA))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca ya esta ocupada para esta funcion.");
        }
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
