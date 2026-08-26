package repository;

import modelo.Funcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FuncionRepository extends JpaRepository<Funcion, Integer> {
    List<Funcion> findBySalaIdAndFecha(int salaId, LocalDate fecha);
}
