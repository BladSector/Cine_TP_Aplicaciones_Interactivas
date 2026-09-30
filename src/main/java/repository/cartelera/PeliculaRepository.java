package repository.cartelera;

import modelo.entidades.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {
}
