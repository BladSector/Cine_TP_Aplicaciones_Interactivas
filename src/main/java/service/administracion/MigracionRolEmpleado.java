package service.administracion;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MigracionRolEmpleado implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public MigracionRolEmpleado(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.update("UPDATE empleado SET rol = 'EMPLEADO' WHERE rol <> 'EMPLEADO'");
        jdbcTemplate.execute("""
                ALTER TABLE sala
                MODIFY COLUMN estado VARCHAR(40) NOT NULL DEFAULT 'DISPONIBLE'
                """);
        jdbcTemplate.update("""
                UPDATE sala
                SET estado = 'PROBLEMA_PARTICULAR',
                    detalle_estado = COALESCE(detalle_estado, 'Problema registrado antes de la actualización')
                WHERE estado = 'FUERA_DE_SERVICIO'
                """);
    }
}
