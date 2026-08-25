package controller;

import modelo.Sala;
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
import service.SalaService;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {
    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public List<SalaResponse> listar() {
        return salaService.listar().stream()
                .map(SalaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public SalaResponse buscarPorId(@PathVariable int id) {
        return SalaResponse.desde(salaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse guardar(@RequestBody SalaRequest request) {
        return SalaResponse.desde(salaService.guardar(request.nombre(), request.capacidad()));
    }

    @PostMapping("/con-butacas")
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse guardarConButacas(@RequestBody SalaConButacasRequest request) {
        return SalaResponse.desde(salaService.guardarConButacas(
                request.nombre(),
                request.filas(),
                request.butacasPorFila()
        ));
    }

    @PostMapping("/con-matriz")
    @ResponseStatus(HttpStatus.CREATED)
    public SalaResponse guardarConMatriz(@RequestBody SalaConMatrizRequest request) {
        return SalaResponse.desde(salaService.guardarConMatriz(request.nombre(), request.butacas()));
    }

    @PutMapping("/{id}")
    public SalaResponse actualizar(@PathVariable int id, @RequestBody SalaRequest request) {
        return SalaResponse.desde(salaService.actualizar(id, request.nombre(), request.capacidad()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        salaService.eliminar(id);
    }

    public record SalaRequest(String nombre, int capacidad) {
    }

    public record SalaConButacasRequest(String nombre, int filas, int butacasPorFila) {
    }

    public record SalaConMatrizRequest(String nombre, List<SalaService.UbicacionButaca> butacas) {
    }

    public record SalaResponse(int id, String nombre, int capacidad) {
        public static SalaResponse desde(Sala sala) {
            return new SalaResponse(sala.getId(), sala.getNombre(), sala.getCapacidad());
        }
    }
}
