package repository;

import modelo.MetodoDePago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoDePagoRepository extends JpaRepository<MetodoDePago, Integer> {
}
