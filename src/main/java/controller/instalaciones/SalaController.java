package controller.instalaciones;

import jakarta.servlet.http.HttpSession;
import modelo.enums.Permiso;
import modelo.entidades.Sala;
import modelo.enums.EstadoSala;
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
import service.instalaciones.SalaService;
import service.usuarios.SesionService;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {
    private final SalaService salaService;
    private final SesionService sesionService;

    public SalaController(SalaService salaService, SesionService sesionService) {
        this.salaService = salaService;
        this.sesionService = sesionService;
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

    @PutMapping("/{id}/matriz")
    public SalaResponse actualizarConMatriz(@PathVariable int id, @RequestBody SalaMatrizUpdateRequest request) {
        return SalaResponse.desde(salaService.actualizarConMatriz(id, request.nombre(), request.butacasActivasIds()));
    }

    @PutMapping("/{id}/distribucion")
    public SalaResponse actualizarDistribucion(@PathVariable int id,
                                                @RequestBody SalaConMatrizRequest request) {
        return SalaResponse.desde(salaService.actualizarDistribucion(id, request.nombre(), request.butacas()));
    }

    @PutMapping("/{id}/estado")
    public SalaResponse cambiarEstado(@PathVariable int id, @RequestBody EstadoSalaRequest request,
                                      HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.GESTIONAR_SALAS);
        return SalaResponse.desde(salaService.cambiarEstado(
                id,
                request.estado(),
                request.descripcion(),
                sesionService.obtenerEmpleadoId(sesion),
                request.destinatarioEmpleadoId()
        ));
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

    public record SalaMatrizUpdateRequest(String nombre, List<Integer> butacasActivasIds) {
    }

    public record EstadoSalaRequest(EstadoSala estado, String descripcion,
                                    Integer destinatarioEmpleadoId) {
    }

    public record SalaResponse(int id, String nombre, int capacidad, EstadoSala estado,
                               String detalleEstado) {
        public static SalaResponse desde(Sala sala) {
            return new SalaResponse(
                    sala.getId(),
                    sala.getNombre(),
                    sala.getCapacidad(),
                    sala.getEstado(),
                    sala.getDetalleEstado()
            );
        }
    }
}
