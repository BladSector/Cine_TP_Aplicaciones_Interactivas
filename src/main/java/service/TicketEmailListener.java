package service;

import modelo.Ticket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import repository.TicketRepository;

@Service
public class TicketEmailListener {
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final EmailService emailService;

    public TicketEmailListener(TicketRepository ticketRepository,
                               TicketService ticketService,
                               EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarTicket(TicketCompradoEvent event) {
        ticketRepository.findById(event.ticketId()).ifPresent(ticket -> {
            try {
                emailService.enviarTicket(ticket, ticketService.calcularTotal(ticket));
            } catch (RuntimeException e) {
                System.err.println("No se pudo enviar el ticket " + event.ticketId() + ": " + e.getMessage());
            }
        });
    }
}
