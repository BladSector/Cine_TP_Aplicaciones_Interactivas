package repository.usuarios;

import modelo.entidades.MetodoDePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoDePagoRepository extends JpaRepository<MetodoDePago, Integer> {
    List<MetodoDePago> findByEspectadorIdAndNumero(int espectadorId, String numero);
}
