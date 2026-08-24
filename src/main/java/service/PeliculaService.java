package service;

import modelo.Categoria;
import modelo.Pelicula;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.CategoriaRepository;
import repository.PeliculaRepository;

import java.util.List;

@Service
public class PeliculaService {
    private final PeliculaRepository peliculaRepository;
    private final CategoriaRepository categoriaRepository;

    public PeliculaService(PeliculaRepository peliculaRepository, CategoriaRepository categoriaRepository) {
        this.peliculaRepository = peliculaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Pelicula> listar() {
        return peliculaRepository.findAll();
    }

    public Pelicula buscarPorId(int id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una pelicula con ese id."));
    }

    public Pelicula guardar(String titulo, int duracion, int categoriaId) {
        try {
            Categoria categoria = buscarCategoria(categoriaId);
            Pelicula pelicula = new Pelicula(titulo, duracion, categoria);
            return peliculaRepository.save(pelicula);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public Pelicula actualizar(int id, String titulo, int duracion, int categoriaId) {
        try {
            Pelicula pelicula = buscarPorId(id);
            Categoria categoria = buscarCategoria(categoriaId);
            pelicula.actualizarDatos(titulo, duracion, categoria);
            return peliculaRepository.save(pelicula);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        Pelicula pelicula = buscarPorId(id);
        peliculaRepository.delete(pelicula);
    }

    private Categoria buscarCategoria(int id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una categoria con ese id."));
    }
}
