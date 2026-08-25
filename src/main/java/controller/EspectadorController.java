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
import service.EspectadorService;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/espectadores")
public class EspectadorController {
    private final EspectadorService espectadorService;

    public EspectadorController(EspectadorService espectadorService) {
        this.espectadorService = espectadorService;
    }

    @GetMapping
    public List<EspectadorResponse> listar() {
        return espectadorService.listar().stream()
                .map(EspectadorResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public EspectadorResponse buscarPorId(@PathVariable int id) {
        return EspectadorResponse.desde(espectadorService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspectadorResponse guardar(@RequestBody EspectadorRequest request) {
        return EspectadorResponse.desde(espectadorService.guardar(
                request.nombre(),
                request.apellido(),
                request.email(),
                request.contrasenia()
        ));
    }

    @PutMapping("/{id}")
    public EspectadorResponse actualizar(@PathVariable int id, @RequestBody EspectadorRequest request) {
        return EspectadorResponse.desde(espectadorService.actualizar(
                id,
                request.nombre(),
                request.apellido(),
                request.email(),
                request.contrasenia()
        ));
    }

    @PutMapping("/{id}/metodo-pago/{metodoDePagoId}")
    public EspectadorResponse asociarMetodoDePago(@PathVariable int id, @PathVariable int metodoDePagoId) {
        return EspectadorResponse.desde(espectadorService.asociarMetodoDePago(id, metodoDePagoId));
    }

    @PutMapping("/{id}/verificar-mail")
    public EspectadorResponse verificarMail(@PathVariable int id) {
        return EspectadorResponse.desde(espectadorService.verificarMail(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        espectadorService.eliminar(id);
    }

    public record EspectadorRequest(String nombre, String apellido, String email, String contrasenia) {
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
                    espectador.getMetodosDePago().stream().map(MetodoDePagoResumen::desde).toList(),
                    espectador.getCantidadEntradas()
            );
        }
    }

    public record MetodoDePagoResumen(int id, String ultimosNumeros, YearMonth fechaVencimiento, String nombre, String apellido) {
        public static MetodoDePagoResumen desde(MetodoDePago metodoDePago) {
            return new MetodoDePagoResumen(
                    metodoDePago.getId(),
                    metodoDePago.getUltimosNumeros(),
                    metodoDePago.getFechaVencimiento(),
                    metodoDePago.getNombre(),
                    metodoDePago.getApellido()
            );
        }
    }
}
