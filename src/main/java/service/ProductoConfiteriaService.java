package service;

import modelo.ProductoConfiteria;
import modelo.TamanoProductoConfiteria;
import modelo.TipoProductoConfiteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.ProductoConfiteriaRepository;

import java.util.List;

@Service
public class ProductoConfiteriaService {
    private final ProductoConfiteriaRepository productoConfiteriaRepository;

    public ProductoConfiteriaService(ProductoConfiteriaRepository productoConfiteriaRepository) {
        this.productoConfiteriaRepository = productoConfiteriaRepository;
    }

    public List<ProductoConfiteria> listar() {
        return productoConfiteriaRepository.findAll();
    }

    public ProductoConfiteria buscarPorId(int id) {
        return productoConfiteriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un producto de confiteria con ese id."));
    }

    public ProductoConfiteria guardar(String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        try {
            return productoConfiteriaRepository.save(new ProductoConfiteria(nombre, precio, tipo, tamano));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public ProductoConfiteria actualizar(int id, String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        try {
            ProductoConfiteria producto = buscarPorId(id);
            producto.actualizarDatos(nombre, precio, tipo, tamano);
            return productoConfiteriaRepository.save(producto);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        ProductoConfiteria producto = buscarPorId(id);
        productoConfiteriaRepository.delete(producto);
    }
}
