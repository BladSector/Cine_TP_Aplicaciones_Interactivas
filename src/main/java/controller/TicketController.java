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
        return TicketResponse.desde(ticketService.guardar(request.espectadorId(), request.metodoDePagoId()));
    }

    @PostMapping("/{id}/entradas/{entradaId}")
    public TicketResponse agregarEntrada(@PathVariable int id, @PathVariable int entradaId) {
        return TicketResponse.desde(ticketService.agregarEntrada(id, entradaId));
    }

    @PostMapping("/{id}/items/{itemId}")
    public TicketResponse agregarItem(@PathVariable int id, @PathVariable int itemId) {
        return TicketResponse.desde(ticketService.agregarItem(id, itemId));
    }

    @PostMapping("/{id}/enviar-mail")
    public TicketResponse enviarPorMail(@PathVariable int id) {
        return TicketResponse.desde(ticketService.enviarPorMail(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        ticketService.eliminar(id);
    }

    public record TicketRequest(int espectadorId, Integer metodoDePagoId) {
    }

    public record TicketResponse(int id, int espectadorId, String espectadorNombre, String codigoQR,
                                 Integer metodoDePagoId, String metodoDePagoResumen,
                                 List<EntradaDetalle> entradas, List<ItemConsumoDetalle> itemsConsumo, double total) {
        public static TicketResponse desde(Ticket ticket) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getEspectador().getId(),
                    ticket.getEspectador().getNombre() + " " + ticket.getEspectador().getApellido(),
                    ticket.getCodigoQR(),
                    null,
                    ticket.getMetodoDePagoResumen(),
                    ticket.getEntradas().stream().map(EntradaDetalle::desde).toList(),
                    ticket.getItemsConsumo().stream().map(ItemConsumoDetalle::desde).toList(),
                    ticket.calcularTotal()
            );
        }
    }

    public record EntradaDetalle(int id, String pelicula, String sala, String butaca,
                                 String fecha, String horario, String formato, String idioma, double precio) {
        public static EntradaDetalle desde(Entrada entrada) {
            return new EntradaDetalle(
                    entrada.getId(),
                    entrada.getFuncion().getPelicula().getTitulo(),
                    entrada.getFuncion().getSala().getNombre(),
                    entrada.getButaca().getFila() + entrada.getButaca().getNumero(),
                    entrada.getFuncion().getFecha().toString(),
                    entrada.getFuncion().getHorario().toString(),
                    entrada.getFuncion().getFormato().name(),
                    entrada.getFuncion().getIdioma().name(),
                    entrada.getPrecio()
            );
        }
    }

    public record ItemConsumoDetalle(int id, String producto, int cantidad, double subtotal) {
        public static ItemConsumoDetalle desde(ItemConsumo itemConsumo) {
            return new ItemConsumoDetalle(
                    itemConsumo.getId(),
                    itemConsumo.getProducto().getNombre(),
                    itemConsumo.getCantidad(),
                    itemConsumo.calcularSubtotal()
            );
        }
    }
}
