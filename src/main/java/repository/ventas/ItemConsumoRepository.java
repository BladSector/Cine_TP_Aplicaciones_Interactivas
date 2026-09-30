package repository.ventas;

import modelo.entidades.ItemConsumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemConsumoRepository extends JpaRepository<ItemConsumo, Integer> {
}
