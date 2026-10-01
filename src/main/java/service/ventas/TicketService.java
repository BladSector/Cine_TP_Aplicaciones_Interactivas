package service.ventas;

import service.comunicacion.EmailService;
import service.comunicacion.TicketPdfService;
import service.usuarios.MetodoDePagoService;

import modelo.entidades.Entrada;
import modelo.entidades.Espectador;
import modelo.entidades.ItemConsumo;
import modelo.enums.EstadoConsumo;
import modelo.enums.EstadoEntrada;
import modelo.enums.EstadoTicket;
import modelo.entidades.MetodoDePago;
import modelo.entidades.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.ventas.EntradaRepository;
import repository.usuarios.EspectadorRepository;
import repository.ventas.ItemConsumoRepository;
import repository.ventas.TicketRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EspectadorRepository espectadorRepository;
    private final EntradaRepository entradaRepository;
    private final ItemConsumoRepository itemConsumoRepository;
    private final MetodoDePagoService metodoDePagoService;
    private final EmailService emailService;
    private final EntradaService entradaService;
    private final ItemConsumoService itemConsumoService;
    private final TicketPdfService ticketPdfService;

    public TicketService(TicketRepository ticketRepository,
                         EspectadorRepository espectadorRepository,
                         EntradaRepository entradaRepository,
                         ItemConsumoRepository itemConsumoRepository,
                         MetodoDePagoService metodoDePagoService,
                         EmailService emailService,
                         EntradaService entradaService,
                         ItemConsumoService itemConsumoService,
                         TicketPdfService ticketPdfService) {
        this.ticketRepository = ticketRepository;
        this.espectadorRepository = espectadorRepository;
        this.entradaRepository = entradaRepository;
        this.itemConsumoRepository = itemConsumoRepository;
        this.metodoDePagoService = metodoDePagoService;
        this.emailService = emailService;
        this.entradaService = entradaService;
        this.itemConsumoService = itemConsumoService;
        this.ticketPdfService = ticketPdfService;
    }

    public List<Ticket> listar() {
        return ticketRepository.findAll();
    }

    public List<Ticket> listarPorEspectador(int espectadorId) {
        return ticketRepository.findByEspectadorId(espectadorId);
    }

    public Ticket buscarPorId(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ticket con ese id."));
    }

    public Ticket buscarPorCodigoQR(String codigoQR) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El codigo QR es obligatorio.");
        }
        return ticketRepository.findByCodigoQR(codigoQR.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ticket con ese codigo QR."));
    }

    @Transactional
    public Ticket validarEntradas(int id) {
        Ticket ticket = buscarPorId(id);
        if (ticket.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ticket no contiene entradas.");
        }
        boolean tieneEntradaInvalida = ticket.getEntradas().stream()
                .anyMatch(entrada -> entrada.getEstado() != EstadoEntrada.PAGADA
                        && entrada.getEstado() != EstadoEntrada.ESCANEADA);
        if (tieneEntradaInvalida) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ticket contiene entradas que no se pueden validar."
            );
        }
        boolean tieneEntradasPendientes = ticket.getEntradas().stream()
                .anyMatch(entrada -> entrada.getEstado() == EstadoEntrada.PAGADA);
        if (!tieneEntradasPendientes) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Las entradas del ticket ya fueron validadas.");
        }

        ticket.getEntradas().stream()
                .filter(entrada -> entrada.getEstado() == EstadoEntrada.PAGADA)
                .forEach(entrada -> entradaService.escanear(entrada.getId()));
        return ticket;
    }

    @Transactional
    public Ticket entregarConsumos(int id) {
        Ticket ticket = buscarPorId(id);
        if (ticket.getItemsConsumo().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ticket no contiene consumos.");
        }
        boolean tieneConsumoCancelado = ticket.getItemsConsumo().stream()
                .anyMatch(item -> item.getEstado() == EstadoConsumo.CANCELADO);
        if (tieneConsumoCancelado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ticket contiene consumos cancelados."
            );
        }
        boolean tieneConsumosPendientes = ticket.getItemsConsumo().stream()
                .anyMatch(item -> item.getEstado() == EstadoConsumo.PENDIENTE);
        if (!tieneConsumosPendientes) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Los consumos del ticket ya fueron entregados.");
        }

        ticket.getItemsConsumo().stream()
                .filter(item -> item.getEstado() == EstadoConsumo.PENDIENTE)
                .forEach(item -> itemConsumoService.entregar(item.getId()));
        return ticket;
    }

    // Un solo escaneo puede validar las entradas y entregar los consumos pendientes del ticket.
    @Transactional
    public Ticket procesarIngreso(int id) {
        Ticket ticket = buscarPorId(id);
        if (ticket.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ticket no contiene entradas.");
        }

        boolean tieneEntradaInvalida = ticket.getEntradas().stream()
                .anyMatch(entrada -> entrada.getEstado() != EstadoEntrada.PAGADA
                        && entrada.getEstado() != EstadoEntrada.ESCANEADA);
        if (tieneEntradaInvalida) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ticket contiene entradas que no se pueden validar."
            );
        }

        boolean tieneEntradasPendientes = ticket.getEntradas().stream()
                .anyMatch(entrada -> entrada.getEstado() == EstadoEntrada.PAGADA);
        boolean tieneConsumosPendientes = ticket.getItemsConsumo().stream()
                .anyMatch(item -> item.getEstado() == EstadoConsumo.PENDIENTE);
        if (!tieneEntradasPendientes && !tieneConsumosPendientes) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ticket ya fue procesado.");
        }

        ticket.getEntradas().stream()
                .filter(entrada -> entrada.getEstado() == EstadoEntrada.PAGADA)
                .forEach(entrada -> entradaService.escanear(entrada.getId()));
        ticket.getItemsConsumo().stream()
                .filter(item -> item.getEstado() == EstadoConsumo.PENDIENTE)
                .forEach(item -> itemConsumoService.entregar(item.getId()));
        return ticket;
    }

    @Transactional
    public Ticket reembolsar(int id) {
        Ticket ticket = buscarPorId(id);
        if (ticket.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ticket no contiene entradas.");
        }
        if (ticket.getEntradas().stream().allMatch(
                entrada -> entrada.getEstado() == EstadoEntrada.REEMBOLSADA)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ticket ya fue reembolsado.");
        }
        if (ticket.getEntradas().stream().anyMatch(
                entrada -> entrada.getEstado() == EstadoEntrada.ESCANEADA)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede reembolsar un ticket con entradas ya validadas."
            );
        }
        if (ticket.getItemsConsumo().stream().anyMatch(
                item -> item.getEstado() == EstadoConsumo.ENTREGADO)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede reembolsar un ticket con consumos ya entregados."
            );
        }

        ticket.getEntradas().stream()
                .filter(entrada -> entrada.getEstado() != EstadoEntrada.REEMBOLSADA)
                .forEach(entrada -> entradaService.reembolsar(entrada.getId()));
        ticket.getItemsConsumo().stream()
                .filter(item -> item.getEstado() == EstadoConsumo.PENDIENTE)
                .forEach(item -> itemConsumoService.cancelar(item.getId()));
        return ticket;
    }

    public Ticket guardar(int espectadorId) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un metodo de pago.");
    }

    @Transactional
    public Ticket guardar(int espectadorId, Integer metodoDePagoId) {
        Espectador espectador = buscarEspectador(espectadorId);
        validarEmailVerificado(espectador);
        if (metodoDePagoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un metodo de pago.");
        }
        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);
        return crearTicket(espectador, metodoDePago);
    }

    @Transactional
    public Ticket agregarEntrada(int ticketId, int entradaId) {
        Ticket ticket = buscarPorId(ticketId);
        Entrada entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una entrada con ese id."));
        if (entrada.getEspectador().getId() != ticket.getEspectador().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada pertenece a otro espectador.");
        }
        if (entrada.getTicket() != null && entrada.getTicket().getId() != ticketId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada ya pertenece a otro ticket.");
        }
        ticket.agregarEntrada(entrada);
        entradaRepository.save(entrada);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket agregarItem(int ticketId, int itemId) {
        Ticket ticket = buscarPorId(ticketId);
        ItemConsumo itemConsumo = itemConsumoRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un item de consumo con ese id."));
        if (itemConsumo.getTicket() != null && itemConsumo.getTicket().getId() != ticketId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El item ya pertenece a otro ticket.");
        }
        ticket.agregarItem(itemConsumo);
        itemConsumoRepository.save(itemConsumo);
        return ticketRepository.save(ticket);
    }

    public Ticket enviarPorMail(int id) {
        Ticket ticket = buscarPorId(id);
        emailService.enviarTicket(ticket, calcularTotal(ticket));
        return ticket;
    }

    public void eliminar(int id) {
        Ticket ticket = buscarPorId(id);
        if (!ticket.getEntradas().isEmpty() || !ticket.getItemsConsumo().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede eliminar un ticket con compras asociadas.");
        }
        ticketRepository.delete(ticket);
    }

    public Ticket crearTicket(Espectador espectador, MetodoDePago metodoDePago) {
        validarEmailVerificado(espectador);
        metodoDePagoService.validarParaCompra(metodoDePago.getId(), espectador);
        String codigoQr = "TCK-" + UUID.randomUUID();
        return ticketRepository.save(new Ticket(
                espectador,
                metodoDePago,
                metodoDePagoService.crearResumen(metodoDePago),
                codigoQr
        ));
    }

    public double calcularTotal(Ticket ticket) {
        double totalEntradas = ticket.getEntradas().stream().mapToDouble(Entrada::getPrecio).sum();
        double totalConsumos = ticket.getItemsConsumo().stream()
                .mapToDouble(item -> item.getProducto().getPrecio() * item.getCantidad())
                .sum();
        return totalEntradas + totalConsumos;
    }

    public double calcularSubtotal(ItemConsumo item) {
        return item.getProducto().getPrecio() * item.getCantidad();
    }

    // El estado se deriva de entradas y consumos para no duplicar información persistida.
    public EstadoTicket calcularEstado(Ticket ticket) {
        boolean todasReembolsadas = ticket.getEntradas().stream()
                .allMatch(entrada -> entrada.getEstado() == EstadoEntrada.REEMBOLSADA);
        if (!ticket.getEntradas().isEmpty() && todasReembolsadas) {
            return EstadoTicket.REEMBOLSADO;
        }

        boolean todasEscaneadas = ticket.getEntradas().stream()
                .allMatch(entrada -> entrada.getEstado() == EstadoEntrada.ESCANEADA);
        if (!ticket.getEntradas().isEmpty() && todasEscaneadas) {
            boolean consumosPendientes = ticket.getItemsConsumo().stream()
                    .anyMatch(item -> item.getEstado() == EstadoConsumo.PENDIENTE);
            return consumosPendientes ? EstadoTicket.CONSUMOS_PENDIENTES : EstadoTicket.PROCESADO;
        }
        return EstadoTicket.PENDIENTE_DE_INGRESO;
    }

    public byte[] generarPdf(int id) {
        Ticket ticket = buscarPorId(id);
        return ticketPdfService.generar(ticket, calcularTotal(ticket));
    }

    private Espectador buscarEspectador(int id) {
        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
    }

    private void validarEmailVerificado(Espectador espectador) {
        if (!espectador.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe verificar el email antes de comprar.");
        }
    }
}
