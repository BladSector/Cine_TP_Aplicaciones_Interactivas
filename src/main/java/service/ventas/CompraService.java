package service.ventas;

import service.cartelera.PrecioService;
import service.comunicacion.TicketCompradoEvent;
import service.usuarios.MetodoDePagoService;

import modelo.entidades.Butaca;
import modelo.entidades.Entrada;
import modelo.entidades.Espectador;
import modelo.entidades.Funcion;
import modelo.entidades.ItemConsumo;
import modelo.entidades.MetodoDePago;
import modelo.entidades.ProductoConfiteria;
import modelo.entidades.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.instalaciones.ButacaRepository;
import repository.usuarios.EspectadorRepository;
import repository.cartelera.FuncionRepository;
import repository.ventas.ItemConsumoRepository;
import repository.ventas.ProductoConfiteriaRepository;
import repository.ventas.TicketRepository;

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
    private final ReservaButacaService reservaButacaService;
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
                         ReservaButacaService reservaButacaService,
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
        this.reservaButacaService = reservaButacaService;
        this.eventPublisher = eventPublisher;
    }

    // La compra es atómica: si falla una entrada o un consumo, no queda un ticket incompleto.
    @Transactional
    public Ticket comprar(int espectadorId, int metodoDePagoId, int funcionId,
                          List<Integer> butacasIds, List<ItemCompra> items,
                          boolean guardarMetodoPago) {
        validarButacasSolicitadas(butacasIds);

        Espectador espectador = buscarEspectadorVerificado(espectadorId);

        Funcion funcion = funcionRepository.findById(funcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una funcion con ese id."));
        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);

        // El orden estable reduce bloqueos cruzados cuando dos compras compiten por las mismas butacas.
        List<Integer> idsOrdenados = butacasIds.stream().sorted().toList();
        List<Butaca> butacas = butacaRepository.buscarPorIdsConBloqueo(idsOrdenados);
        if (butacas.size() != idsOrdenados.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Una o mas butacas no existen.");
        }
        reservaButacaService.validarDisponibilidadParaCompra(funcion, espectador, butacas);

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

        reservaButacaService.liberarTrasCompra(funcion, espectador, butacas);

        Ticket ticketGuardado = ticketRepository.save(ticket);
        desactivarMetodoTemporal(metodoDePago, guardarMetodoPago);
        // El correo se procesa después del commit para no informar una compra que luego se revierta.
        eventPublisher.publishEvent(new TicketCompradoEvent(ticketGuardado.getId()));
        return ticketGuardado;
    }

    @Transactional
    public Ticket comprarConsumibles(int espectadorId, int metodoDePagoId, List<ItemCompra> items,
                                     boolean guardarMetodoPago) {
        validarItemsSolicitados(items);

        Espectador espectador = buscarEspectadorVerificado(espectadorId);
        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);
        Ticket ticket = ticketService.crearTicket(espectador, metodoDePago);

        for (ItemCompra itemCompra : items) {
            agregarItem(ticket, itemCompra);
        }

        Ticket ticketGuardado = ticketRepository.save(ticket);
        desactivarMetodoTemporal(metodoDePago, guardarMetodoPago);
        eventPublisher.publishEvent(new TicketCompradoEvent(ticketGuardado.getId()));
        return ticketGuardado;
    }

    private void desactivarMetodoTemporal(MetodoDePago metodoDePago, boolean guardarMetodoPago) {
        if (!guardarMetodoPago) {
            metodoDePagoService.eliminar(metodoDePago.getId());
        }
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

    private void validarItemsSolicitados(List<ItemCompra> items) {
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar al menos un producto del Candy."
            );
        }
        Set<Integer> productosUnicos = new HashSet<>();
        for (ItemCompra item : items) {
            if (item == null || item.cantidad() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La cantidad de cada consumible debe ser mayor a 0."
                );
            }
            if (!productosUnicos.add(item.productoId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No se puede incluir dos veces el mismo producto."
                );
            }
        }
    }

    private Espectador buscarEspectadorVerificado(int espectadorId) {
        Espectador espectador = espectadorRepository.findById(espectadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un espectador con ese id."
                ));
        if (!espectador.isEmailVerificado()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe verificar el email antes de comprar."
            );
        }
        return espectador;
    }

    public record ItemCompra(int productoId, int cantidad) {
    }
}
