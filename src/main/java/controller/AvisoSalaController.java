package controller;

import jakarta.servlet.http.HttpSession;
import modelo.AvisoSala;
import modelo.EstadoSala;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import service.AvisoSalaService;
import service.SesionService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/avisos-sala")
public class AvisoSalaController {
    private final AvisoSalaService avisoSalaService;
    private final SesionService sesionService;

    public AvisoSalaController(AvisoSalaService avisoSalaService, SesionService sesionService) {
        this.avisoSalaService = avisoSalaService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<AvisoSalaResponse> listarHistorial(HttpSession sesion) {
        int empleadoId = requerirEmpleado(sesion);
        return avisoSalaService.listarHistorial(empleadoId).stream()
                .map(AvisoSalaResponse::desde)
                .toList();
    }

    @GetMapping("/pendientes")
    public List<AvisoSalaResponse> listarPendientes(HttpSession sesion) {
        int empleadoId = requerirEmpleado(sesion);
        return avisoSalaService.listarPendientes(empleadoId).stream()
                .map(AvisoSalaResponse::desde)
                .toList();
    }

    @PutMapping("/{id}/leer")
    public AvisoSalaResponse marcarLeido(@PathVariable long id, HttpSession sesion) {
        return AvisoSalaResponse.desde(avisoSalaService.marcarLeido(id, requerirEmpleado(sesion)));
    }

    private int requerirEmpleado(HttpSession sesion) {
        Integer empleadoId = sesionService.obtenerEmpleadoId(sesion);
        if (empleadoId == null || !sesionService.esEmpleado(sesion)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Se requiere una sesión de empleado."
            );
        }
        return empleadoId;
    }

    public record AvisoSalaResponse(long id, int salaId, String salaNombre, EstadoSala estado,
                                    String descripcion, String creadoPorUsuario,
                                    String destinatarioUsuario, LocalDateTime fechaHora,
                                    boolean leido) {
        public static AvisoSalaResponse desde(AvisoSala aviso) {
            return new AvisoSalaResponse(
                    aviso.getId(),
                    aviso.getSala().getId(),
                    aviso.getSala().getNombre(),
                    aviso.getEstado(),
                    aviso.getDescripcion(),
                    aviso.getCreadoPorUsuario(),
                    aviso.getDestinatarioUsuario(),
                    aviso.getFechaHora(),
                    aviso.isLeido()
            );
        }
    }
}
