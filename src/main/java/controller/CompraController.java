package controller;

import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.CompraService;
import service.TicketService;
import service.SesionService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/compras")
public class CompraController {
    private final CompraService compraService;
    private final TicketService ticketService;
    private final SesionService sesionService;

    public CompraController(CompraService compraService, TicketService ticketService, SesionService sesionService) {
        this.compraService = compraService;
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
        List<TicketController.ItemConsumoDetalle> items = ticket.getItemsConsumo().stream()
                .map(item -> TicketController.ItemConsumoDetalle.desde(item, ticketService.calcularSubtotal(item)))
                .toList();
        return TicketController.TicketResponse.desde(ticket, ticketService.calcularTotal(ticket), items);
    }

    public record CompraRequest(int espectadorId, int metodoDePagoId, int funcionId,
                                List<Integer> butacasIds, List<CompraService.ItemCompra> items) {
    }
}
