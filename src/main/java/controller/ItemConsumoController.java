package controller;

import modelo.ItemConsumo;
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

import java.util.List;

@RestController
@RequestMapping("/items-consumo")
public class ItemConsumoController {
    private final ItemConsumoService itemConsumoService;

    public ItemConsumoController(ItemConsumoService itemConsumoService) {
        this.itemConsumoService = itemConsumoService;
    }

    @GetMapping
    public List<ItemConsumoResponse> listar() {
        return itemConsumoService.listar().stream()
                .map(ItemConsumoResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ItemConsumoResponse buscarPorId(@PathVariable int id) {
        return ItemConsumoResponse.desde(itemConsumoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemConsumoResponse guardar(@RequestBody ItemConsumoRequest request) {
        return ItemConsumoResponse.desde(itemConsumoService.guardar(
                request.productoId(),
                request.cantidad(),
                request.ticketId()
        ));
    }

    @PutMapping("/{id}")
    public ItemConsumoResponse actualizar(@PathVariable int id, @RequestBody ItemConsumoRequest request) {
        return ItemConsumoResponse.desde(itemConsumoService.actualizar(id, request.productoId(), request.cantidad()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        itemConsumoService.eliminar(id);
    }

    public record ItemConsumoRequest(int productoId, int cantidad, Integer ticketId) {
    }

    public record ItemConsumoResponse(int id, int productoId, String productoNombre,
                                      int cantidad, Integer ticketId, double subtotal) {
        public static ItemConsumoResponse desde(ItemConsumo item) {
            Integer ticketId = item.getTicket() == null ? null : item.getTicket().getId();
            return new ItemConsumoResponse(
                    item.getId(),
                    item.getProducto().getId(),
                    item.getProducto().getNombre(),
                    item.getCantidad(),
                    ticketId,
                    item.calcularSubtotal()
            );
        }
    }
}
