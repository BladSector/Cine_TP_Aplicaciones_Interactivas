package controller.ventas;

import modelo.entidades.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.ventas.CompraService;
import service.ventas.MembresiaService;
import service.ventas.TicketService;
import service.usuarios.SesionService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraController {
    private final CompraService compraService;
    private final MembresiaService membresiaService;
    private final TicketService ticketService;
    private final SesionService sesionService;

    public CompraController(CompraService compraService, MembresiaService membresiaService,
                            TicketService ticketService, SesionService sesionService) {
        this.compraService = compraService;
        this.membresiaService = membresiaService;
        this.ticketService = ticketService;
        this.sesionService = sesionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketController.TicketResponse comprar(@RequestBody CompraRequest request, HttpSession sesion) {
        sesionService.validarEspectador(sesion, request.espectadorId());
        Ticket ticket = compraService.comprar(
                request.espectadorId(),
                request.metodoDePagoId(),
                request.funcionId(),
                request.butacasIds(),
                request.items()
        );
        return crearResponse(ticket);
    }

    @PostMapping("/candy")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketController.TicketResponse comprarCandy(@RequestBody CompraCandyRequest request,
                                                         HttpSession sesion) {
        sesionService.validarEspectador(sesion, request.espectadorId());
        Ticket ticket = compraService.comprarConsumibles(
                request.espectadorId(),
                request.metodoDePagoId(),
                request.items()
        );
        return crearResponse(ticket);
    }

    @PostMapping("/membresia")
    @ResponseStatus(HttpStatus.CREATED)
    public MembresiaController.SuscripcionResponse comprarMembresia(@RequestBody CompraMembresiaRequest request,
                                                                     HttpSession sesion) {
        sesionService.validarEspectador(sesion, request.espectadorId());
        return MembresiaController.SuscripcionResponse.desde(membresiaService.comprar(
                request.espectadorId(),
                request.metodoDePagoId(),
                request.membresiaId()
        ));
    }

    private TicketController.TicketResponse crearResponse(Ticket ticket) {
        List<TicketController.ItemConsumoDetalle> items = ticket.getItemsConsumo().stream()
                .map(item -> TicketController.ItemConsumoDetalle.desde(item, ticketService.calcularSubtotal(item)))
                .toList();
        return TicketController.TicketResponse.desde(
                ticket,
                ticketService.calcularEstado(ticket),
                ticketService.calcularTotal(ticket),
                items
        );
    }

    public record CompraRequest(int espectadorId, int metodoDePagoId, int funcionId,
                                List<Integer> butacasIds, List<CompraService.ItemCompra> items) {
    }

    public record CompraCandyRequest(int espectadorId, int metodoDePagoId,
                                     List<CompraService.ItemCompra> items) {
    }

    public record CompraMembresiaRequest(int espectadorId, int metodoDePagoId, int membresiaId) {
    }
}
