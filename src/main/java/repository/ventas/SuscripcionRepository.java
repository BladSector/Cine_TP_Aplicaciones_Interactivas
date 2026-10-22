package repository.ventas;

import modelo.entidades.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {
    Optional<Suscripcion> findFirstByEspectadorIdAndActivaTrueOrderByFechaFinDesc(int espectadorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Suscripcion s WHERE s.espectador.id = :espectadorId AND s.activa = true")
    List<Suscripcion> bloquearActivas(@Param("espectadorId") int espectadorId);
}
