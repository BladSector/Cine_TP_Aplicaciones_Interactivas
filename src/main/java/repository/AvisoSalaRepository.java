package repository;

import modelo.AvisoSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvisoSalaRepository extends JpaRepository<AvisoSala, Long> {
    List<AvisoSala> findByDestinatarioEmpleadoIdAndLeidoFalseOrderByFechaHoraAsc(int empleadoId);

    List<AvisoSala> findByDestinatarioEmpleadoIdOrderByFechaHoraDesc(int empleadoId);
}
