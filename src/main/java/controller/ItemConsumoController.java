package controller;

import modelo.ItemConsumo;
import modelo.EstadoConsumo;
import modelo.Permiso;
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
import service.ItemConsumoService;
import service.SesionService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/items-consumo")
public class ItemConsumoController {
    private final ItemConsumoService itemConsumoService;
    private final SesionService sesionService;

    public ItemConsumoController(ItemConsumoService itemConsumoService, SesionService sesionService) {
        this.itemConsumoService = itemConsumoService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<ItemConsumoResponse> listar(HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.OPERAR_POS);
        return itemConsumoService.listar().stream()
                .map(this::crearResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ItemConsumoResponse buscarPorId(@PathVariable int id, HttpSession sesion) {
        sesionService.validarPermiso(sesion, Permiso.OPERAR_POS);
        return crearResponse(itemConsumoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemConsumoResponse guardar(@RequestBody ItemConsumoRequest request) {
        return crearResponse(itemConsumoService.guardar(
                request.productoId(),
                request.cantidad(),
                request.ticketId()
        ));
    }

    @PutMapping("/{id}")
    public ItemConsumoResponse actualizar(@PathVariable int id, @RequestBody ItemConsumoRequest request) {
        return crearResponse(itemConsumoService.actualizar(id, request.productoId(), request.cantidad()));
    }

    @PutMapping("/{id}/entregar")
    public ItemConsumoResponse entregar(@PathVariable int id) {
        return crearResponse(itemConsumoService.entregar(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        itemConsumoService.eliminar(id);
    }

    private ItemConsumoResponse crearResponse(ItemConsumo item) {
        return ItemConsumoResponse.desde(item, itemConsumoService.calcularSubtotal(item));
    }

    public record ItemConsumoRequest(int productoId, int cantidad, Integer ticketId) {
    }

    public record ItemConsumoResponse(int id, int productoId, String productoNombre,
                                      int cantidad, Integer ticketId, EstadoConsumo estado, double subtotal) {
        public static ItemConsumoResponse desde(ItemConsumo item, double subtotal) {
            Integer ticketId = item.getTicket() == null ? null : item.getTicket().getId();
            return new ItemConsumoResponse(
                    item.getId(),
                    item.getProducto().getId(),
                    item.getProducto().getNombre(),
                    item.getCantidad(),
                    ticketId,
                    item.getEstado(),
                    subtotal
            );
        }
    }
}
