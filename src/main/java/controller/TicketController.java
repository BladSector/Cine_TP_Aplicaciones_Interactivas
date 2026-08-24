package controller;

import modelo.Entrada;
import modelo.ItemConsumo;
import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.TicketService;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> listar() {
        return ticketService.listar().stream()
                .map(TicketResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public TicketResponse buscarPorId(@PathVariable int id) {
        return TicketResponse.desde(ticketService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse guardar(@RequestBody TicketRequest request) {
        return TicketResponse.desde(ticketService.guardar(request.espectadorId()));
    }

    @PostMapping("/{id}/entradas/{entradaId}")
    public TicketResponse agregarEntrada(@PathVariable int id, @PathVariable int entradaId) {
        return TicketResponse.desde(ticketService.agregarEntrada(id, entradaId));
    }

    @PostMapping("/{id}/items/{itemId}")
    public TicketResponse agregarItem(@PathVariable int id, @PathVariable int itemId) {
        return TicketResponse.desde(ticketService.agregarItem(id, itemId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        ticketService.eliminar(id);
    }

    public record TicketRequest(int espectadorId) {
    }

    public record TicketResponse(int id, int espectadorId, String codigoQR,
                                 List<Integer> entradasIds, List<Integer> itemsConsumoIds, double total) {
        public static TicketResponse desde(Ticket ticket) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getEspectador().getId(),
                    ticket.getCodigoQR(),
                    ticket.getEntradas().stream().map(Entrada::getId).toList(),
                    ticket.getItemsConsumo().stream().map(ItemConsumo::getId).toList(),
                    ticket.calcularTotal()
            );
        }
    }
}
