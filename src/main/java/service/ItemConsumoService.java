package service;

import modelo.ItemConsumo;
import modelo.ProductoConfiteria;
import modelo.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ItemConsumoRepository;
import repository.ProductoConfiteriaRepository;
import repository.TicketRepository;

import java.util.List;

@Service
public class ItemConsumoService {
    private final ItemConsumoRepository itemConsumoRepository;
    private final ProductoConfiteriaRepository productoConfiteriaRepository;
    private final TicketRepository ticketRepository;

    public ItemConsumoService(ItemConsumoRepository itemConsumoRepository,
                              ProductoConfiteriaRepository productoConfiteriaRepository,
                              TicketRepository ticketRepository) {
        this.itemConsumoRepository = itemConsumoRepository;
        this.productoConfiteriaRepository = productoConfiteriaRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<ItemConsumo> listar() {
        return itemConsumoRepository.findAll();
    }

    public ItemConsumo buscarPorId(int id) {
        return itemConsumoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un item de consumo con ese id."));
    }

    public ItemConsumo guardar(int productoId, int cantidad, Integer ticketId) {
        try {
            ProductoConfiteria producto = buscarProducto(productoId);
            ItemConsumo item = new ItemConsumo(producto, cantidad);

            if (ticketId != null) {
                Ticket ticket = buscarTicket(ticketId);
                ticket.agregarItem(item);
            }

            return itemConsumoRepository.save(item);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public ItemConsumo actualizar(int id, int productoId, int cantidad) {
        try {
            ItemConsumo item = buscarPorId(id);
            ProductoConfiteria producto = buscarProducto(productoId);
            item.actualizarDatos(producto, cantidad);
            return itemConsumoRepository.save(item);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        ItemConsumo item = buscarPorId(id);
        itemConsumoRepository.delete(item);
    }

    private ProductoConfiteria buscarProducto(int id) {
        return productoConfiteriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un producto de confiteria con ese id."));
    }

    private Ticket buscarTicket(int id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un ticket con ese id."));
    }
}
