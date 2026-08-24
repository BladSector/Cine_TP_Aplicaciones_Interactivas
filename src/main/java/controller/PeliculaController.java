package controller;

import modelo.Pelicula;
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
import service.PeliculaService;

import java.util.List;

@RestController
@RequestMapping("/peliculas")
public class PeliculaController {
    private final PeliculaService peliculaService;

    public PeliculaController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @GetMapping
    public List<PeliculaResponse> listar() {
        return peliculaService.listar().stream()
                .map(PeliculaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PeliculaResponse buscarPorId(@PathVariable int id) {
        return PeliculaResponse.desde(peliculaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PeliculaResponse guardar(@RequestBody PeliculaRequest request) {
        return PeliculaResponse.desde(peliculaService.guardar(request.titulo(), request.duracion(), request.categoriaId()));
    }

    @PutMapping("/{id}")
    public PeliculaResponse actualizar(@PathVariable int id, @RequestBody PeliculaRequest request) {
        return PeliculaResponse.desde(peliculaService.actualizar(id, request.titulo(), request.duracion(), request.categoriaId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        peliculaService.eliminar(id);
    }

    public record PeliculaRequest(String titulo, int duracion, int categoriaId) {
    }

    public record PeliculaResponse(int id, String titulo, int duracion, int categoriaId, String categoriaNombre) {
        public static PeliculaResponse desde(Pelicula pelicula) {
            return new PeliculaResponse(
                    pelicula.getId(),
                    pelicula.getTitulo(),
                    pelicula.getDuracion(),
                    pelicula.getCategoria().getId(),
                    pelicula.getCategoria().getNombre()
            );
        }
    }
}
