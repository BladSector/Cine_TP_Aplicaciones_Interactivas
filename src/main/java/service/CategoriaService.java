package service;

import modelo.Categoria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.CategoriaRepository;

import java.util.List;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(int id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una categoria con ese id."));
    }

    public Categoria guardar(String nombre) {
        validarNombre(nombre);
        return categoriaRepository.save(new Categoria(nombre.trim()));
    }

    public Categoria actualizar(int id, String nombre) {
        validarNombre(nombre);
        Categoria categoria = buscarPorId(id);
        categoria.actualizarNombre(nombre.trim());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(int id) {
        Categoria categoria = buscarPorId(id);
        categoriaRepository.delete(categoria);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la categoria es obligatorio.");
        }
    }
}
