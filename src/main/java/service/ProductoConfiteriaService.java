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
        validarDatos(nombre, precio, tipo, tamano);
        return productoConfiteriaRepository.save(new ProductoConfiteria(nombre.trim(), precio, tipo, tamano));
    }

    public ProductoConfiteria actualizar(int id, String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        validarDatos(nombre, precio, tipo, tamano);
        ProductoConfiteria producto = buscarPorId(id);
        producto.actualizarDatos(nombre.trim(), precio, tipo, tamano);
        return productoConfiteriaRepository.save(producto);
    }

    public void eliminar(int id) {
        ProductoConfiteria producto = buscarPorId(id);
        productoConfiteriaRepository.delete(producto);
    }

    private void validarDatos(String nombre, double precio, TipoProductoConfiteria tipo, TamanoProductoConfiteria tamano) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto es obligatorio.");
        }
        if (precio <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio del producto debe ser mayor a 0.");
        }
        if (tipo == null || tamano == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo y el tamaño del producto son obligatorios.");
        }
    }
}
