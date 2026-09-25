package controller;

import modelo.Espectador;
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
import org.springframework.web.bind.annotation.RequestParam;
import service.EspectadorService;
import service.SesionService;
import jakarta.servlet.http.HttpSession;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/espectadores")
public class EspectadorController {
    private final EspectadorService espectadorService;
    private final SesionService sesionService;

    public EspectadorController(EspectadorService espectadorService, SesionService sesionService) {
        this.espectadorService = espectadorService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<EspectadorResponse> listar(HttpSession sesion) {
        sesionService.validarAdministrador(sesion);
        return espectadorService.listar().stream()
                .map(EspectadorResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public EspectadorResponse buscarPorId(@PathVariable int id, HttpSession sesion) {
        validarPropietarioOAdmin(sesion, id);
        return EspectadorResponse.desde(espectadorService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspectadorResponse guardar(@RequestBody EspectadorRequest request, HttpSession sesion) {
        Espectador espectador = espectadorService.guardar(
                request.nombre(),
                request.apellido(),
                request.email(),
                request.contrasenia(),
                request.contraseniaConfirmacion()
        );
        sesionService.iniciarEspectador(sesion, espectador.getId());
        return EspectadorResponse.desde(espectador);
    }

    @PostMapping("/login")
    public EspectadorResponse login(@RequestBody LoginRequest request, HttpSession sesion) {
        Espectador espectador = espectadorService.autenticar(request.email(), request.contrasenia());
        sesionService.iniciarEspectador(sesion, espectador.getId());
        return EspectadorResponse.desde(espectador);
    }

    @PostMapping("/solicitar-recuperacion")
    public RecuperacionResponse solicitarRecuperacion(@RequestBody RecuperacionEmailRequest request) {
        EspectadorService.SolicitudRecuperacion solicitud = espectadorService.solicitarRecuperacionContrasenia(request.email());
        return new RecuperacionResponse(solicitud.espectador().getEmail(), solicitud.token());
    }

    @PutMapping("/{id}")
    public EspectadorResponse actualizar(@PathVariable int id, @RequestBody EspectadorRequest request, HttpSession sesion) {
        validarPropietarioOAdmin(sesion, id);
        return EspectadorResponse.desde(espectadorService.actualizar(
                id,
                request.nombre(),
                request.apellido(),
                request.email(),
                request.contraseniaActual(),
                request.nuevaContrasenia(),
                request.nuevaContraseniaConfirmacion()
        ));
    }

    @PostMapping("/recuperar-contrasenia")
    public EspectadorResponse recuperarContrasenia(@RequestBody RecuperarContraseniaRequest request) {
        return EspectadorResponse.desde(espectadorService.recuperarContrasenia(
                request.email(),
                request.token(),
                request.nuevaContrasenia(),
                request.nuevaContraseniaConfirmacion()
        ));
    }

    @PutMapping("/{id}/metodo-pago/{metodoDePagoId}")
    public EspectadorResponse asociarMetodoDePago(@PathVariable int id, @PathVariable int metodoDePagoId,
                                                   HttpSession sesion) {
        sesionService.validarEspectador(sesion, id);
        return EspectadorResponse.desde(espectadorService.asociarMetodoDePago(id, metodoDePagoId));
    }

    @PutMapping("/{id}/verificar-mail")
    public EspectadorResponse verificarMail(@PathVariable int id) {
        return EspectadorResponse.desde(espectadorService.verificarMail(id));
    }

    @GetMapping("/verificar-mail")
    public EspectadorResponse verificarMailConToken(@RequestParam String token) {
        return EspectadorResponse.desde(espectadorService.verificarMail(token));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id, HttpSession sesion) {
        validarPropietarioOAdmin(sesion, id);
        espectadorService.eliminar(id);
    }

    private void validarPropietarioOAdmin(HttpSession sesion, int espectadorId) {
        if (!sesionService.esAdministrador(sesion)) {
            sesionService.validarEspectador(sesion, espectadorId);
        }
    }

    public record EspectadorRequest(String nombre, String apellido, String email,
                                    String contrasenia, String contraseniaConfirmacion,
                                    String contraseniaActual, String nuevaContrasenia,
                                    String nuevaContraseniaConfirmacion) {
    }

    public record LoginRequest(String email, String contrasenia) {
    }

    public record RecuperacionEmailRequest(String email) {
    }

    public record RecuperarContraseniaRequest(String email, String token, String nuevaContrasenia, String nuevaContraseniaConfirmacion) {
    }

    public record RecuperacionResponse(String email, String token) {
    }

    public record EspectadorResponse(int id, String nombre, String apellido, String email,
                                     boolean emailVerificado, Integer metodoDePagoId,
                                     List<MetodoDePagoResumen> metodosDePago, int cantidadEntradas) {
        public static EspectadorResponse desde(Espectador espectador) {
            Integer metodoDePagoId = espectador.getMetodoDePago() == null ? null : espectador.getMetodoDePago().getId();
            return new EspectadorResponse(
                    espectador.getId(),
                    espectador.getNombre(),
                    espectador.getApellido(),
                    espectador.getEmail(),
                    espectador.isEmailVerificado(),
                    metodoDePagoId,
                    espectador.getMetodosDePagoActivos().stream().map(MetodoDePagoResumen::desde).toList(),
                    espectador.getCantidadEntradas()
            );
        }
    }

    public record MetodoDePagoResumen(int id, String ultimosNumeros, YearMonth fechaVencimiento,
                                      String titular, String nombre, String apellido) {
        public static MetodoDePagoResumen desde(MetodoDePago metodoDePago) {
            return new MetodoDePagoResumen(
                    metodoDePago.getId(),
                    metodoDePago.getUltimosNumeros(),
                    metodoDePago.getFechaVencimiento(),
                    metodoDePago.getTitular(),
                    metodoDePago.getNombre(),
                    metodoDePago.getApellido()
            );
        }
    }
}
