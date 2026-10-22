package service.ventas;

import modelo.entidades.Membresia;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import repository.ventas.MembresiaRepository;

/**
 * Carga los tres planes del seed cuando la tabla está vacía.
 * El script completo sigue siendo database/seed_demo.sql.
 */
@Component
public class MembresiaInicializador implements ApplicationRunner {
    private final MembresiaRepository membresiaRepository;

    public MembresiaInicializador(MembresiaRepository membresiaRepository) {
        this.membresiaRepository = membresiaRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (membresiaRepository.count() > 0) {
            return;
        }
        membresiaRepository.save(plan(
                "Luneta",
                7900,
                "Para sumar cine al mes sin pensarlo demasiado.",
                """
                2 entradas 2D por mes
                10% de descuento en el Candy
                Preventa con 48 horas de anticipación
                """,
                false
        ));
        membresiaRepository.save(plan(
                "Platea",
                13500,
                "La butaca que elegís vos, con un combo incluido.",
                """
                4 entradas 2D por mes
                1 combo clásico por mes
                20% de descuento en el Candy
                Preventa con 4 días de anticipación
                Upgrade a butaca central cuando hay lugar
                """,
                true
        ));
        membresiaRepository.save(plan(
                "Palco",
                21900,
                "Estrenos, invitados y la noche completa.",
                """
                Entradas 2D ilimitadas de lunes a jueves
                2 entradas de invitado por mes
                Candy bonificado en cada función
                Preventa de una semana y avant premiere
                Estacionamiento incluido
                """,
                false
        ));
    }

    private Membresia plan(String nombre, double precio, String descripcion, String beneficios, boolean destacada) {
        return new Membresia(nombre, precio, descripcion, beneficios.strip(), destacada);
    }
}
