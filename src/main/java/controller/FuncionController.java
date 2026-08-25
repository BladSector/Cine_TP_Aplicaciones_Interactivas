package controller;

import modelo.FormatoFuncion;
import modelo.Funcion;
import modelo.IdiomaFuncion;
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
import service.FuncionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/funciones")
public class FuncionController {
    private final FuncionService funcionService;

    public FuncionController(FuncionService funcionService) {
        this.funcionService = funcionService;
    }

    @GetMapping
    public List<FuncionResponse> listar() {
        return funcionService.listar().stream()
                .map(FuncionResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public FuncionResponse buscarPorId(@PathVariable int id) {
        return FuncionResponse.desde(funcionService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionResponse guardar(@RequestBody FuncionRequest request) {
        return FuncionResponse.desde(funcionService.guardar(
                request.fecha(),
                request.horario(),
                request.peliculaId(),
                request.salaId(),
                request.formato(),
                request.idioma(),
                request.precioEntrada()
        ));
    }

    @PutMapping("/{id}")
    public FuncionResponse actualizar(@PathVariable int id, @RequestBody FuncionRequest request) {
        return FuncionResponse.desde(funcionService.actualizar(
                id,
                request.fecha(),
                request.horario(),
                request.peliculaId(),
                request.salaId(),
                request.formato(),
                request.idioma(),
                request.precioEntrada()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable int id) {
        funcionService.eliminar(id);
    }

    public record FuncionRequest(LocalDate fecha, LocalTime horario, int peliculaId, int salaId, FormatoFuncion formato, IdiomaFuncion idioma, double precioEntrada) {
    }

    public record FuncionResponse(int id, LocalDate fecha, LocalTime horario, FormatoFuncion formato, IdiomaFuncion idioma,
                                  double precioEntrada, int peliculaId, String peliculaTitulo, int salaId, String salaNombre) {
        public static FuncionResponse desde(Funcion funcion) {
            return new FuncionResponse(
                    funcion.getId(),
                    funcion.getFecha(),
                    funcion.getHorario(),
                    funcion.getFormato(),
                    funcion.getIdioma(),
                    funcion.getPrecioEntrada(),
                    funcion.getPelicula().getId(),
                    funcion.getPelicula().getTitulo(),
                    funcion.getSala().getId(),
                    funcion.getSala().getNombre()
            );
        }
    }
}
