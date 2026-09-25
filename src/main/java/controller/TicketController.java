package controller;

import modelo.Entrada;
import modelo.ItemConsumo;
import modelo.EstadoConsumo;
import modelo.EstadoEntrada;
import modelo.Permiso;
import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.TicketService;
import service.SesionService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final SesionService sesionService;

    public TicketController(TicketService ticketService, SesionService sesionService) {
        this.ticketService = ticketService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<TicketResponse> listar(HttpSession sesion) {
        List<Ticket> tickets = puedeOperarTickets(sesion)
                ? ticketService.listar()
                : ticketService.listarPorEspectador(requerirEspectador(sesion));
        return tickets.stream()
                .map(this::crearResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TicketResponse buscarPorId(@PathVariable int id, HttpSession sesion) {
        Ticket ticket = ticketService.buscarPorId(id);
        validarPropietarioOAdmin(ticket, sesion);
        return crearResponse(ticket);
    }

    @GetMapping("/qr/{codigoQR}")
    public TicketResponse buscarPorCodigoQR(@PathVariable String codigoQR, HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.VALIDAR_TICKETS);
        return crearResponse(ticketService.buscarPorCodigoQR(codigoQR));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable int id, HttpSession sesion) {
        Ticket ticket = ticketService.buscarPorId(id);
        validarPropietarioOAdmin(ticket, sesion);
        byte[] pdf = ticketService.generarPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + id + ".pdf")
                .body(pdf);
    }

    @PutMapping("/{id}/validar-entradas")
    public TicketResponse validarEntradas(@PathVariable int id, HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.VALIDAR_TICKETS);
        return crearResponse(ticketService.validarEntradas(id));
    }

    @PutMapping("/{id}/entregar-consumos")
    public TicketResponse entregarConsumos(@PathVariable int id, HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.OPERAR_POS);
        return crearResponse(ticketService.entregarConsumos(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse guardar(@RequestBody TicketRequest request) {
        return crearResponse(ticketService.guardar(request.espectadorId(), request.metodoDePagoId()));
    }

    @PostMapping("/{id}/entradas/{entradaId}")
    public TicketResponse agregarEntrada(@PathVariable int id, @PathVariable int entradaId) {
        return crearResponse(ticketService.agregarEntrada(id, entradaId));
    }

    @PostMapping("/{id}/items/{itemId}")
    public TicketResponse agregarItem(@PathVariable int id, @PathVariable int itemId) {
        return crearResponse(ticketService.agregarItem(id, itemId));
    }

    @PostMapping("/{id}/enviar-mail")
    public TicketResponse enviarPorMail(@PathVariable int id) {
        return crearResponse(ticketService.enviarPorMail(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        ticketService.eliminar(id);
    }

    public TicketResponse crearResponse(Ticket ticket) {
        List<ItemConsumoDetalle> items = ticket.getItemsConsumo().stream()
                .map(item -> ItemConsumoDetalle.desde(item, ticketService.calcularSubtotal(item)))
                .toList();
        return TicketResponse.desde(ticket, ticketService.calcularTotal(ticket), items);
    }

    private int requerirEspectador(HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesion.");
        }
        return espectadorId;
    }

    private void validarPropietarioOAdmin(Ticket ticket, HttpSession sesion) {
        if (!puedeOperarTickets(sesion)) {
            sesionService.validarEspectador(sesion, ticket.getEspectador().getId());
        }
    }

    private boolean puedeOperarTickets(HttpSession sesion) {
        return sesionService.tienePermiso(sesion, Permiso.VALIDAR_TICKETS)
                || sesionService.tienePermiso(sesion, Permiso.OPERAR_POS);
    }

    public record TicketRequest(int espectadorId, Integer metodoDePagoId) {
    }

    public record TicketResponse(int id, int espectadorId, String espectadorNombre, String codigoQR,
                                 Integer metodoDePagoId, String metodoDePagoResumen,
                                 List<EntradaDetalle> entradas, List<ItemConsumoDetalle> itemsConsumo, double total) {
        public static TicketResponse desde(Ticket ticket, double total, List<ItemConsumoDetalle> itemsConsumo) {
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getEspectador().getId(),
                    ticket.getEspectador().getNombre() + " " + ticket.getEspectador().getApellido(),
                    ticket.getCodigoQR(),
                    null,
                    ticket.getMetodoDePagoResumen(),
                    ticket.getEntradas().stream().map(EntradaDetalle::desde).toList(),
                    itemsConsumo,
                    total
            );
        }
    }

    public record EntradaDetalle(int id, String pelicula, String sala, String butaca, EstadoEntrada estado,
                                 String fecha, String horario, String formato, String idioma, double precio) {
        public static EntradaDetalle desde(Entrada entrada) {
            return new EntradaDetalle(
                    entrada.getId(),
                    entrada.getFuncion().getPelicula().getTitulo(),
                    entrada.getFuncion().getSala().getNombre(),
                    entrada.getButaca().getFila() + entrada.getButaca().getNumero(),
                    entrada.getEstado(),
                    entrada.getFuncion().getFecha().toString(),
                    entrada.getFuncion().getHorario().toString(),
                    entrada.getFuncion().getFormato().name(),
                    entrada.getFuncion().getIdioma().name(),
                    entrada.getPrecio()
            );
        }
    }

    public record ItemConsumoDetalle(int id, String producto, int cantidad, EstadoConsumo estado, double subtotal) {
        public static ItemConsumoDetalle desde(ItemConsumo itemConsumo, double subtotal) {
            return new ItemConsumoDetalle(
                    itemConsumo.getId(),
                    itemConsumo.getProducto().getNombre(),
                    itemConsumo.getCantidad(),
                    itemConsumo.getEstado(),
                    subtotal
            );
        }
    }
}
