package controller;

import modelo.Categoria;
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
import service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable int id) {
        return CategoriaResponse.desde(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse guardar(@RequestBody CategoriaRequest request) {
        return CategoriaResponse.desde(categoriaService.guardar(request.nombre()));
    }

    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable int id, @RequestBody CategoriaRequest request) {
        return CategoriaResponse.desde(categoriaService.actualizar(id, request.nombre()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        categoriaService.eliminar(id);
    }

    public record CategoriaRequest(String nombre) {
    }

    public record CategoriaResponse(int id, String nombre) {
        public static CategoriaResponse desde(Categoria categoria) {
            return new CategoriaResponse(categoria.getId(), categoria.getNombre());
        }
    }
}
