package repository.ventas;

import modelo.entidades.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembresiaRepository extends JpaRepository<Membresia, Integer> {
    List<Membresia> findAllByOrderByPrecioMensualAsc();
}
