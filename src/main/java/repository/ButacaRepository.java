package repository;

import modelo.Butaca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;

public interface ButacaRepository extends JpaRepository<Butaca, Integer> {
    List<Butaca> findBySalaId(int salaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Butaca b WHERE b.id IN :ids ORDER BY b.id")
    List<Butaca> buscarPorIdsConBloqueo(@Param("ids") List<Integer> ids);
}
