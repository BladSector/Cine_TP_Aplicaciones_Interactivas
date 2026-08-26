package service;

import modelo.Entrada;
import modelo.Espectador;
import modelo.ItemConsumo;
import modelo.MetodoDePago;
import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EntradaRepository;
import repository.EspectadorRepository;
import repository.ItemConsumoRepository;
import repository.MetodoDePagoRepository;
import repository.TicketRepository;

import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EspectadorRepository espectadorRepository;
    private final EntradaRepository entradaRepository;
    private final ItemConsumoRepository itemConsumoRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final EmailService emailService;

    public TicketService(TicketRepository ticketRepository,
                         EspectadorRepository espectadorRepository,
                         EntradaRepository entradaRepository,
                         ItemConsumoRepository itemConsumoRepository,
                         MetodoDePagoRepository metodoDePagoRepository,
                         EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.espectadorRepository = espectadorRepository;
        this.entradaRepository = entradaRepository;
        this.itemConsumoRepository = itemConsumoRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
        this.emailService = emailService;
    }

    public List<Ticket> listar() {
        return ticketRepository.findAll();
    }

    public Ticket buscarPorId(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ticket con ese id."));
    }

    public Ticket guardar(int espectadorId) {
        return guardar(espectadorId, null);
    }

    public Ticket guardar(int espectadorId, Integer metodoDePagoId) {
        try {
            Espectador espectador = espectadorRepository.findById(espectadorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
            MetodoDePago metodoDePago = null;

            if (metodoDePagoId != null) {
                metodoDePago = metodoDePagoRepository.findById(metodoDePagoId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));

                if (metodoDePago.getEspectador() == null || metodoDePago.getEspectador().getId() != espectador.getId()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago no pertenece al espectador.");
                }

                if (!metodoDePago.isActiva()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago ya no esta activo.");
                }

                if (metodoDePago.estaVencido()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago esta vencido.");
                }
            }

            String metodoDePagoResumen = metodoDePago == null
                    ? null
                    : "Tarjeta terminada en " + metodoDePago.getUltimosNumeros();

            return ticketRepository.save(new Ticket(espectador, metodoDePagoResumen));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Ticket agregarEntrada(int ticketId, int entradaId) {
        try {
            Ticket ticket = buscarPorId(ticketId);
            Entrada entrada = entradaRepository.findById(entradaId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una entrada con ese id."));
            ticket.agregarEntrada(entrada);
            entradaRepository.save(entrada);
            return ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Ticket agregarItem(int ticketId, int itemId) {
        try {
            Ticket ticket = buscarPorId(ticketId);
            ItemConsumo itemConsumo = itemConsumoRepository.findById(itemId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un item de consumo con ese id."));
            ticket.agregarItem(itemConsumo);
            itemConsumoRepository.save(itemConsumo);
            return ticketRepository.save(ticket);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Ticket enviarPorMail(int id) {
        Ticket ticket = buscarPorId(id);
        emailService.enviarTicket(ticket);
        return ticket;
    }

    public void eliminar(int id) {
        Ticket ticket = buscarPorId(id);
        ticketRepository.delete(ticket);
    }
}
