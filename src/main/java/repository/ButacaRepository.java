package repository;

import modelo.Butaca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ButacaRepository extends JpaRepository<Butaca, Integer> {
    List<Butaca> findBySalaId(int salaId);
}
