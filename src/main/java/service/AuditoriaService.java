package service;

import jakarta.servlet.http.HttpSession;
import modelo.Auditoria;
import modelo.Empleado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.AuditoriaRepository;
import repository.EmpleadoRepository;

import java.util.List;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final SesionService sesionService;

    public AuditoriaService(AuditoriaRepository auditoriaRepository,
                            EmpleadoRepository empleadoRepository,
                            SesionService sesionService) {
        this.auditoriaRepository = auditoriaRepository;
        this.empleadoRepository = empleadoRepository;
        this.sesionService = sesionService;
    }

    public List<Auditoria> listar() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    public Actor obtenerActor(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        try {
            if (sesionService.esDuenio(sesion)) {
                return new Actor(null, "admin", "DUEÑO");
            }

            Integer empleadoId = sesionService.obtenerEmpleadoId(sesion);
            if (empleadoId == null) {
                return null;
            }
            String usuario = empleadoRepository.findById(empleadoId)
                    .map(Empleado::getUsuario)
                    .orElse("empleado-" + empleadoId);
            return new Actor(empleadoId, usuario, "EMPLEADO");
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Actor actor, String accion, String entidad, Integer entidadId,
                          String detalle, String resultado) {
        if (actor == null) {
            return;
        }
        auditoriaRepository.save(new Auditoria(
                actor.empleadoId(),
                actor.usuario(),
                actor.tipoActor(),
                accion,
                entidad,
                entidadId,
                detalle,
                resultado
        ));
    }

    public record Actor(Integer empleadoId, String usuario, String tipoActor) {
    }
}
