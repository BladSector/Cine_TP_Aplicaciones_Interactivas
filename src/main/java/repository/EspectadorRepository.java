package repository;

import modelo.Espectador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EspectadorRepository extends JpaRepository<Espectador, Integer> {
    Optional<Espectador> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Espectador> findByTokenVerificacionEmail(String token);
    Optional<Espectador> findByTokenRecuperacionContrasenia(String token);
}
