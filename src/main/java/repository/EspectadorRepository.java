package repository;

import modelo.Espectador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspectadorRepository extends JpaRepository<Espectador, Integer> {
    Optional<Espectador> findByEmail(String email);
}
