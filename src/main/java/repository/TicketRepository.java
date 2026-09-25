package repository;

import modelo.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByEspectadorId(int espectadorId);

    Optional<Ticket> findByCodigoQR(String codigoQR);
}
