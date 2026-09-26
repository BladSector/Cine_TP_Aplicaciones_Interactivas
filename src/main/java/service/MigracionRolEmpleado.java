package service;

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
    }
}
