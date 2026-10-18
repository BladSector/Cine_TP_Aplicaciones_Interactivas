package repository.ventas;

import modelo.entidades.ReservaButaca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaButacaRepository extends JpaRepository<ReservaButaca, Long> {
    Optional<ReservaButaca> findByFuncionIdAndButacaId(int funcionId, int butacaId);

    List<ReservaButaca> findByFuncionIdOrderByButacaId(int funcionId);

    long deleteByVenceEnLessThanEqual(LocalDateTime momento);
}
