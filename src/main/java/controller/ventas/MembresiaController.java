package controller.ventas;

import modelo.entidades.Membresia;
import modelo.entidades.Suscripcion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import service.usuarios.SesionService;
import service.ventas.MembresiaService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/membresias")
public class MembresiaController {
    private final MembresiaService membresiaService;
    private final SesionService sesionService;

    public MembresiaController(MembresiaService membresiaService, SesionService sesionService) {
        this.membresiaService = membresiaService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<MembresiaResponse> listar() {
        return membresiaService.listar().stream()
                .map(MembresiaResponse::desde)
                .toList();
    }

    @GetMapping("/actual")
    public ResponseEntity<SuscripcionResponse> actual(HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            return ResponseEntity.noContent().build();
        }
        Suscripcion suscripcion = membresiaService.buscarVigente(espectadorId);
        if (suscripcion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(SuscripcionResponse.desde(suscripcion));
    }

    @GetMapping("/{membresiaId}/cotizacion")
    public CotizacionResponse cotizar(@PathVariable int membresiaId, HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión.");
        }
        return CotizacionResponse.desde(membresiaService.cotizar(espectadorId, membresiaId));
    }

    @DeleteMapping("/actual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesion.");
        }
        membresiaService.darDeBaja(espectadorId);
    }

    public record MembresiaResponse(int id, String nombre, double precioMensual, String descripcion,
                                    List<String> beneficios, boolean destacada) {
        public static MembresiaResponse desde(Membresia membresia) {
            return new MembresiaResponse(
                    membresia.getId(),
                    membresia.getNombre(),
                    membresia.getPrecioMensual(),
                    membresia.getDescripcion(),
                    membresia.listarBeneficios(),
                    membresia.isDestacada()
            );
        }
    }

    public record SuscripcionResponse(int id, int membresiaId, String nombre, String descripcion,
                                      List<String> beneficios, double precioMensual, double precioCobrado,
                                      String fechaInicio, String fechaFin, Integer membresiaProgramadaId,
                                      String membresiaProgramadaNombre, Double precioProgramado,
                                      String fechaCambioProgramado) {
        public static SuscripcionResponse desde(Suscripcion suscripcion) {
            Membresia membresia = suscripcion.getMembresia();
            Membresia programada = suscripcion.getMembresiaProgramada();
            return new SuscripcionResponse(
                    suscripcion.getId(),
                    membresia.getId(),
                    membresia.getNombre(),
                    membresia.getDescripcion(),
                    membresia.listarBeneficios(),
                    membresia.getPrecioMensual(),
                    suscripcion.getPrecioCobrado(),
                    suscripcion.getFechaInicio().toString(),
                    suscripcion.getFechaFin().toString(),
                    programada == null ? null : programada.getId(),
                    programada == null ? null : programada.getNombre(),
                    programada == null ? null : programada.getPrecioMensual(),
                    programada == null ? null : suscripcion.getFechaFin().plusDays(1).toString()
            );
        }
    }

    public record CotizacionResponse(String tipo, double importeAhora, double importeRenovacion,
                                     String fechaInicio, String fechaFin, String mensaje) {
        public static CotizacionResponse desde(MembresiaService.Cotizacion cotizacion) {
            return new CotizacionResponse(
                    cotizacion.tipo().name(),
                    cotizacion.importeAhora(),
                    cotizacion.importeRenovacion(),
                    cotizacion.fechaInicio().toString(),
                    cotizacion.fechaFin().toString(),
                    cotizacion.mensaje()
            );
        }
    }
}
