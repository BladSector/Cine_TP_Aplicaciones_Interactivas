package repository.usuarios;

import modelo.entidades.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {
    Optional<Empleado> findByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, int id);

    List<Empleado> findByActivoTrueOrderByNombreAscApellidoAsc();
}
