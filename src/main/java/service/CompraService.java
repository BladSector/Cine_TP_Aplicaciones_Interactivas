package service;

import modelo.Butaca;
import modelo.Entrada;
import modelo.Espectador;
import modelo.Funcion;
import modelo.ItemConsumo;
import modelo.MetodoDePago;
import modelo.ProductoConfiteria;
import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.ButacaRepository;
import repository.EspectadorRepository;
import repository.FuncionRepository;
import repository.ItemConsumoRepository;
import repository.ProductoConfiteriaRepository;
import repository.TicketRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompraService {
    private final EspectadorRepository espectadorRepository;
    private final FuncionRepository funcionRepository;
    private final ButacaRepository butacaRepository;
    private final ProductoConfiteriaRepository productoRepository;
    private final ItemConsumoRepository itemConsumoRepository;
    private final TicketRepository ticketRepository;
    private final MetodoDePagoService metodoDePagoService;
    private final PrecioService precioService;
    private final EntradaService entradaService;
    private final TicketService ticketService;
    private final ApplicationEventPublisher eventPublisher;

    public CompraService(EspectadorRepository espectadorRepository,
                         FuncionRepository funcionRepository,
                         ButacaRepository butacaRepository,
                         ProductoConfiteriaRepository productoRepository,
                         ItemConsumoRepository itemConsumoRepository,
                         TicketRepository ticketRepository,
                         MetodoDePagoService metodoDePagoService,
                         PrecioService precioService,
                         EntradaService entradaService,
                         TicketService ticketService,
                         ApplicationEventPublisher eventPublisher) {
        this.espectadorRepository = espectadorRepository;
        this.funcionRepository = funcionRepository;
        this.butacaRepository = butacaRepository;
        this.productoRepository = productoRepository;
        this.itemConsumoRepository = itemConsumoRepository;
        this.ticketRepository = ticketRepository;
        this.metodoDePagoService = metodoDePagoService;
        this.precioService = precioService;
        this.entradaService = entradaService;
        this.ticketService = ticketService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Ticket comprar(int espectadorId, int metodoDePagoId, int funcionId,
                          List<Integer> butacasIds, List<ItemCompra> items) {
        validarButacasSolicitadas(butacasIds);

        Espectador espectador = espectadorRepository.findById(espectadorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
        if (!espectador.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe verificar el email antes de comprar.");
        }

        Funcion funcion = funcionRepository.findById(funcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una funcion con ese id."));
        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);

        List<Integer> idsOrdenados = butacasIds.stream().sorted().toList();
        List<Butaca> butacas = butacaRepository.buscarPorIdsConBloqueo(idsOrdenados);
        if (butacas.size() != idsOrdenados.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Una o mas butacas no existen.");
        }

        double totalEntradas = precioService.calcularTotal(
                funcion, espectador, funcion.getPrecioEntrada(), butacas.size()
        );
        double precioPorEntrada = totalEntradas / butacas.size();

        Ticket ticket = ticketService.crearTicket(espectador, metodoDePago);
        for (Butaca butaca : butacas) {
            Entrada entrada = entradaService.crearEntradaPagada(funcion, espectador, butaca, precioPorEntrada);
            ticket = ticketService.agregarEntrada(ticket.getId(), entrada.getId());
        }

        if (items != null) {
            for (ItemCompra itemCompra : items) {
                agregarItem(ticket, itemCompra);
            }
        }

        Ticket ticketGuardado = ticketRepository.save(ticket);
        eventPublisher.publishEvent(new TicketCompradoEvent(ticketGuardado.getId()));
        return ticketGuardado;
    }

    private void agregarItem(Ticket ticket, ItemCompra itemCompra) {
        if (itemCompra == null || itemCompra.cantidad() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad de cada consumible debe ser mayor a 0.");
        }
        ProductoConfiteria producto = productoRepository.findById(itemCompra.productoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un producto con ese id."));
        ItemConsumo item = new ItemConsumo(producto, itemCompra.cantidad());
        ticket.agregarItem(item);
        itemConsumoRepository.save(item);
    }

    private void validarButacasSolicitadas(List<Integer> butacasIds) {
        if (butacasIds == null || butacasIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos una butaca.");
        }
        Set<Integer> idsUnicos = new HashSet<>(butacasIds);
        if (idsUnicos.size() != butacasIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede seleccionar dos veces la misma butaca.");
        }
    }

    public record ItemCompra(int productoId, int cantidad) {
    }
}
