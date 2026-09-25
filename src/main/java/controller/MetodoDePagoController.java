package controller;

import modelo.MetodoDePago;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.MetodoDePagoService;
import service.EspectadorService;
import service.SesionService;
import jakarta.servlet.http.HttpSession;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/metodos-pago")
public class MetodoDePagoController {
    private final MetodoDePagoService metodoDePagoService;
    private final EspectadorService espectadorService;
    private final SesionService sesionService;

    public MetodoDePagoController(MetodoDePagoService metodoDePagoService,
                                  EspectadorService espectadorService,
                                  SesionService sesionService) {
        this.metodoDePagoService = metodoDePagoService;
        this.espectadorService = espectadorService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<MetodoDePagoResponse> listar(HttpSession sesion) {
        sesionService.validarAdministrador(sesion);
        return metodoDePagoService.listar().stream()
                .map(MetodoDePagoResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public MetodoDePagoResponse buscarPorId(@PathVariable int id, HttpSession sesion) {
        MetodoDePago metodo = metodoDePagoService.buscarPorId(id);
        validarPropietarioOAdmin(metodo, sesion);
        return MetodoDePagoResponse.desde(metodo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoDePagoResponse guardar(@RequestBody MetodoDePagoRequest request, HttpSession sesion) {
        Integer espectadorId = sesionService.obtenerEspectadorId(sesion);
        if (espectadorId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesion.");
        }
        String titular = obtenerTitular(request);
        MetodoDePago metodo = metodoDePagoService.guardar(
                request.numero(),
                request.fechaVencimiento(),
                titular,
                "",
                request.cvv()
        );
        espectadorService.asociarMetodoDePago(espectadorId, metodo.getId());
        return MetodoDePagoResponse.desde(metodo);
    }

    @PutMapping("/{id}")
    public MetodoDePagoResponse actualizar(@PathVariable int id, @RequestBody MetodoDePagoRequest request,
                                            HttpSession sesion) {
        validarPropietarioOAdmin(metodoDePagoService.buscarPorId(id), sesion);
        String titular = obtenerTitular(request);
        return MetodoDePagoResponse.desde(metodoDePagoService.actualizar(
                id,
                request.numero(),
                request.fechaVencimiento(),
                titular,
                "",
                request.cvv()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id, HttpSession sesion) {
        validarPropietarioOAdmin(metodoDePagoService.buscarPorId(id), sesion);
        metodoDePagoService.eliminar(id);
    }

    private void validarPropietarioOAdmin(MetodoDePago metodo, HttpSession sesion) {
        if (sesionService.esAdministrador(sesion)) {
            return;
        }
        if (metodo.getEspectador() == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "El metodo de pago no tiene propietario.");
        }
        sesionService.validarEspectador(sesion, metodo.getEspectador().getId());
    }

    private String obtenerTitular(MetodoDePagoRequest request) {
        if (request.nombreTitular() != null && !request.nombreTitular().isBlank()) {
            return request.nombreTitular();
        }

        String nombre = request.nombre() == null ? "" : request.nombre();
        String apellido = request.apellido() == null ? "" : request.apellido();

        return (nombre + " " + apellido).trim();
    }

    public record MetodoDePagoRequest(String numero, YearMonth fechaVencimiento,
                                      String nombreTitular, String nombre, String apellido, String cvv) {
    }

    public record MetodoDePagoResponse(int id, String ultimosNumeros, YearMonth fechaVencimiento,
                                       String titular, String nombre, String apellido, boolean activa,
                                       Integer espectadorId, String espectadorNombre) {
        public static MetodoDePagoResponse desde(MetodoDePago metodoDePago) {
            Integer espectadorId = metodoDePago.getEspectador() == null ? null : metodoDePago.getEspectador().getId();
            String espectadorNombre = metodoDePago.getEspectador() == null
                    ? null
                    : metodoDePago.getEspectador().getNombre() + " " + metodoDePago.getEspectador().getApellido();
            return new MetodoDePagoResponse(
                    metodoDePago.getId(),
                    metodoDePago.getUltimosNumeros(),
                    metodoDePago.getFechaVencimiento(),
                    metodoDePago.getTitular(),
                    metodoDePago.getNombre(),
                    metodoDePago.getApellido(),
                    metodoDePago.isActiva(),
                    espectadorId,
                    espectadorNombre
            );
        }
    }
}
