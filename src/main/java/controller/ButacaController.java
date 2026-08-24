package controller;

import modelo.Butaca;
import modelo.EstadoButaca;
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
import service.ButacaService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/butacas")
public class ButacaController {
    private final ButacaService butacaService;

    public ButacaController(ButacaService butacaService) {
        this.butacaService = butacaService;
    }

    @GetMapping
    public List<ButacaResponse> listar() {
        return butacaService.listar().stream()
                .map(ButacaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ButacaResponse buscarPorId(@PathVariable int id) {
        return ButacaResponse.desde(butacaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ButacaResponse guardar(@RequestBody ButacaRequest request) {
        return ButacaResponse.desde(butacaService.guardar(request.fila(), request.numero(), request.salaId()));
    }

    @PutMapping("/{id}")
    public ButacaResponse actualizar(@PathVariable int id, @RequestBody ButacaRequest request) {
        return ButacaResponse.desde(butacaService.actualizar(id, request.fila(), request.numero(), request.salaId()));
    }

    @PutMapping("/{id}/bloquear")
    public ButacaResponse bloquear(@PathVariable int id, @RequestBody BloqueoRequest request) {
        return ButacaResponse.desde(butacaService.bloquear(id, request.minutos()));
    }

    @PutMapping("/{id}/ocupar")
    public ButacaResponse ocupar(@PathVariable int id) {
        return ButacaResponse.desde(butacaService.ocupar(id));
    }

    @PutMapping("/{id}/liberar")
    public ButacaResponse liberar(@PathVariable int id) {
        return ButacaResponse.desde(butacaService.liberar(id));
    }

    @PutMapping("/{id}/fuera-de-servicio")
    public ButacaResponse marcarFueraDeServicio(@PathVariable int id) {
        return ButacaResponse.desde(butacaService.marcarFueraDeServicio(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        butacaService.eliminar(id);
    }

    public record ButacaRequest(String fila, int numero, int salaId) {
    }

    public record BloqueoRequest(int minutos) {
    }

    public record ButacaResponse(int id, String fila, int numero, EstadoButaca estado, LocalDateTime bloqueoHasta, int salaId) {
        public static ButacaResponse desde(Butaca butaca) {
            return new ButacaResponse(
                    butaca.getId(),
                    butaca.getFila(),
                    butaca.getNumero(),
                    butaca.getEstado(),
                    butaca.getBloqueoHasta(),
                    butaca.getSala().getId()
            );
        }
    }
}
