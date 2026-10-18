package controller.ventas;

import jakarta.servlet.http.HttpSession;
import modelo.entidades.ReservaButaca;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.usuarios.SesionService;
import service.ventas.ReservaButacaService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservas-butacas")
public class ReservaButacaController {
    private final ReservaButacaService reservaService;
    private final SesionService sesionService;

    public ReservaButacaController(ReservaButacaService reservaService, SesionService sesionService) {
        this.reservaService = reservaService;
        this.sesionService = sesionService;
    }

    @GetMapping("/funciones/{funcionId}")
    public List<ReservaButacaResponse> listar(@PathVariable int funcionId, HttpSession sesion) {
        int espectadorId = requerirEspectador(sesion);
        return reservaService.listarActivas(funcionId).stream()
                .map(reserva -> ReservaButacaResponse.desde(reserva, espectadorId))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaButacaResponse reservar(@RequestBody ReservaButacaRequest request, HttpSession sesion) {
        int espectadorId = requerirEspectador(sesion);
        return ReservaButacaResponse.desde(
                reservaService.reservar(request.funcionId(), request.butacaId(), espectadorId),
                espectadorId
        );
    }

    @DeleteMapping("/funciones/{funcionId}/butacas/{butacaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@PathVariable int funcionId, @PathVariable int butacaId, HttpSession sesion) {
        reservaService.liberar(funcionId, butacaId, requerirEspectador(sesion));
    }

    private int requerirEspectador(HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debe iniciar sesión."
            );
        }
        return espectadorId;
    }

    public record ReservaButacaRequest(int funcionId, int butacaId) {
    }

    public record ReservaButacaResponse(int butacaId, LocalDateTime venceEn, boolean propia) {
        public static ReservaButacaResponse desde(ReservaButaca reserva, int espectadorId) {
            return new ReservaButacaResponse(
                    reserva.getButaca().getId(),
                    reserva.getVenceEn(),
                    reserva.getEspectador().getId() == espectadorId
            );
        }
    }
}
