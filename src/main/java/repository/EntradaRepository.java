package repository;

import modelo.EstadoEntrada;
import modelo.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntradaRepository extends JpaRepository<Entrada, Integer> {
    @Query("""
            SELECT COUNT(e) > 0
            FROM Entrada e
            WHERE e.funcion.id = :funcionId
            AND e.butaca.id = :butacaId
            AND e.estado NOT IN :estadosLibres
            """)
    boolean existeEntradaActivaParaButaca(@Param("funcionId") int funcionId,
                                          @Param("butacaId") int butacaId,
                                          @Param("estadosLibres") List<EstadoEntrada> estadosLibres);
}
