package controller;

import modelo.Entrada;
import modelo.EstadoEntrada;
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
import service.EntradaService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/entradas")
public class EntradaController {
    private final EntradaService entradaService;

    public EntradaController(EntradaService entradaService) {
        this.entradaService = entradaService;
    }

    @GetMapping
    public List<EntradaResponse> listar() {
        return entradaService.listar().stream()
                .map(EntradaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public EntradaResponse buscarPorId(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaResponse vender(@RequestBody EntradaRequest request) {
        return EntradaResponse.desde(entradaService.vender(
                request.precio(),
                request.espectadorId(),
                request.funcionId(),
                request.butacaId()
        ));
    }

    @PutMapping("/{id}/pendiente-pago")
    public EntradaResponse marcarPendienteDePago(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.marcarPendienteDePago(id));
    }

    @PutMapping("/{id}/pagar")
    public EntradaResponse pagar(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.pagar(id));
    }

    @PutMapping("/{id}/escanear")
    public EntradaResponse escanear(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.escanear(id));
    }

    @PutMapping("/{id}/reembolsar")
    public EntradaResponse reembolsar(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.reembolsar(id));
    }

    @PutMapping("/{id}/cancelar")
    public EntradaResponse cancelar(@PathVariable int id) {
        return EntradaResponse.desde(entradaService.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        entradaService.eliminar(id);
    }

    public record EntradaRequest(double precio, int espectadorId, int funcionId, int butacaId) {
    }

    public record EntradaResponse(int id, double precio, EstadoEntrada estado, LocalDateTime horario,
                                  int espectadorId, int funcionId, int butacaId, Integer ticketId) {
        public static EntradaResponse desde(Entrada entrada) {
            Integer ticketId = entrada.getTicket() == null ? null : entrada.getTicket().getId();
            return new EntradaResponse(
                    entrada.getId(),
                    entrada.getPrecio(),
                    entrada.getEstado(),
                    entrada.getHorario(),
                    entrada.getEspectador().getId(),
                    entrada.getFuncion().getId(),
                    entrada.getButaca().getId(),
                    ticketId
            );
        }
    }
}
