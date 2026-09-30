package repository.usuarios;

import modelo.entidades.MetodoDePago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoDePagoRepository extends JpaRepository<MetodoDePago, Integer> {
}
