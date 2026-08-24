package controller;

import modelo.ProductoConfiteria;
import modelo.TamanoProductoConfiteria;
import modelo.TipoProductoConfiteria;
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
import service.ProductoConfiteriaService;

import java.util.List;

@RestController
@RequestMapping("/productos-confiteria")
public class ProductoConfiteriaController {
    private final ProductoConfiteriaService productoConfiteriaService;

    public ProductoConfiteriaController(ProductoConfiteriaService productoConfiteriaService) {
        this.productoConfiteriaService = productoConfiteriaService;
    }

    @GetMapping
    public List<ProductoConfiteriaResponse> listar() {
        return productoConfiteriaService.listar().stream()
                .map(ProductoConfiteriaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductoConfiteriaResponse buscarPorId(@PathVariable int id) {
        return ProductoConfiteriaResponse.desde(productoConfiteriaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoConfiteriaResponse guardar(@RequestBody ProductoConfiteriaRequest request) {
        return ProductoConfiteriaResponse.desde(productoConfiteriaService.guardar(
                request.nombre(),
                request.precio(),
                request.tipo(),
                request.tamano()
        ));
    }

    @PutMapping("/{id}")
    public ProductoConfiteriaResponse actualizar(@PathVariable int id, @RequestBody ProductoConfiteriaRequest request) {
        return ProductoConfiteriaResponse.desde(productoConfiteriaService.actualizar(
                id,
                request.nombre(),
                request.precio(),
                request.tipo(),
                request.tamano()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        productoConfiteriaService.eliminar(id);
    }

    public record ProductoConfiteriaRequest(String nombre, double precio,
                                            TipoProductoConfiteria tipo,
                                            TamanoProductoConfiteria tamano) {
    }

    public record ProductoConfiteriaResponse(int id, String nombre, double precio,
                                             TipoProductoConfiteria tipo,
                                             TamanoProductoConfiteria tamano) {
        public static ProductoConfiteriaResponse desde(ProductoConfiteria producto) {
            return new ProductoConfiteriaResponse(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getTipo(),
                    producto.getTamano()
            );
        }
    }
}
