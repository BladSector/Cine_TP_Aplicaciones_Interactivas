package repository.instalaciones;

import modelo.entidades.Butaca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ButacaRepository extends JpaRepository<Butaca, Integer> {
    List<Butaca> findBySalaId(int salaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Butaca b WHERE b.sala.id = :salaId ORDER BY b.id")
    List<Butaca> buscarPorSalaConBloqueo(@Param("salaId") int salaId);

    @Query("""
            SELECT COUNT(e) > 0 FROM Entrada e WHERE e.butaca.id = :id
            AND e.estado NOT IN (modelo.enums.EstadoEntrada.REEMBOLSADA, modelo.enums.EstadoEntrada.CANCELADA)
            AND (e.funcion.fecha > :hoy OR (e.funcion.fecha = :hoy AND e.funcion.horario >= :hora))
            """)
    boolean tieneEntradasVigentes(@Param("id") int id, @Param("hoy") java.time.LocalDate hoy,
                                 @Param("hora") java.time.LocalTime hora);

    @Query("SELECT COUNT(r) > 0 FROM ReservaButaca r WHERE r.butaca.id = :id AND r.venceEn > :ahora")
    boolean tieneReservasVigentes(@Param("id") int id, @Param("ahora") java.time.LocalDateTime ahora);

    // Serializa compras simultáneas y evita vender la misma butaca dos veces en una función.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Butaca b WHERE b.id IN :ids ORDER BY b.id")
    List<Butaca> buscarPorIdsConBloqueo(@Param("ids") List<Integer> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Butaca b WHERE b.id = :id")
    Optional<Butaca> buscarPorIdConBloqueo(@Param("id") int id);
}
