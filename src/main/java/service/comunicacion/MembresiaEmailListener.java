package service.comunicacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import repository.ventas.SuscripcionRepository;

@Service
public class MembresiaEmailListener {
    private final SuscripcionRepository suscripcionRepository;
    private final EmailService emailService;

    public MembresiaEmailListener(SuscripcionRepository suscripcionRepository, EmailService emailService) {
        this.suscripcionRepository = suscripcionRepository;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarConfirmacion(MembresiaAdquiridaEvent event) {
        suscripcionRepository.findById(event.suscripcionId()).ifPresent(suscripcion -> {
            try {
                emailService.enviarConfirmacionMembresia(suscripcion);
            } catch (RuntimeException e) {
                System.err.println("No se pudo enviar la membresia " + event.suscripcionId() + ": " + e.getMessage());
            }
        });
    }
}
