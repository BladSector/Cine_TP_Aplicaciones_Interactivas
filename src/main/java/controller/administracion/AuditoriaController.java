package controller.administracion;

import jakarta.servlet.http.HttpSession;
import modelo.entidades.Auditoria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.administracion.AuditoriaService;
import service.usuarios.SesionService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auditorias")
public class AuditoriaController {
    private final AuditoriaService auditoriaService;
    private final SesionService sesionService;

    public AuditoriaController(AuditoriaService auditoriaService, SesionService sesionService) {
        this.auditoriaService = auditoriaService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<AuditoriaResponse> listar(HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return auditoriaService.listar().stream().map(AuditoriaResponse::desde).toList();
    }

    public record AuditoriaResponse(long id, LocalDateTime fechaHora, Integer empleadoId,
                                    String usuario, String tipoActor, String accion,
                                    String entidad, Integer entidadId, String resultado,
                                    String detalle) {
        public static AuditoriaResponse desde(Auditoria auditoria) {
            return new AuditoriaResponse(
                    auditoria.getId(),
                    auditoria.getFechaHora(),
                    auditoria.getEmpleadoId(),
                    auditoria.getUsuario(),
                    auditoria.getTipoActor(),
                    auditoria.getAccion(),
                    auditoria.getEntidad(),
                    auditoria.getEntidadId(),
                    auditoria.getResultado(),
                    auditoria.getDetalle()
            );
        }
    }
}
