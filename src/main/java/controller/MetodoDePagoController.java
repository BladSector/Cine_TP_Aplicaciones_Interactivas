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

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/metodos-pago")
public class MetodoDePagoController {
    private final MetodoDePagoService metodoDePagoService;

    public MetodoDePagoController(MetodoDePagoService metodoDePagoService) {
        this.metodoDePagoService = metodoDePagoService;
    }

    @GetMapping
    public List<MetodoDePagoResponse> listar() {
        return metodoDePagoService.listar().stream()
                .map(MetodoDePagoResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public MetodoDePagoResponse buscarPorId(@PathVariable int id) {
        return MetodoDePagoResponse.desde(metodoDePagoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoDePagoResponse guardar(@RequestBody MetodoDePagoRequest request) {
        return MetodoDePagoResponse.desde(metodoDePagoService.guardar(
                request.numero(),
                request.fechaVencimiento(),
                request.nombre(),
                request.apellido(),
                request.cvv()
        ));
    }

    @PutMapping("/{id}")
    public MetodoDePagoResponse actualizar(@PathVariable int id, @RequestBody MetodoDePagoRequest request) {
        return MetodoDePagoResponse.desde(metodoDePagoService.actualizar(
                id,
                request.numero(),
                request.fechaVencimiento(),
                request.nombre(),
                request.apellido(),
                request.cvv()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        metodoDePagoService.eliminar(id);
    }

    public record MetodoDePagoRequest(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
    }

    public record MetodoDePagoResponse(int id, String numero, YearMonth fechaVencimiento, String nombre, String apellido) {
        public static MetodoDePagoResponse desde(MetodoDePago metodoDePago) {
            return new MetodoDePagoResponse(
                    metodoDePago.getId(),
                    metodoDePago.getNumero(),
                    metodoDePago.getFechaVencimiento(),
                    metodoDePago.getNombre(),
                    metodoDePago.getApellido()
            );
        }
    }
}
