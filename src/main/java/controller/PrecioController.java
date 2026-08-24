package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.PrecioService;

@RestController
@RequestMapping("/precios")
public class PrecioController {
    private final PrecioService precioService;

    public PrecioController(PrecioService precioService) {
        this.precioService = precioService;
    }

    @GetMapping("/entrada")
    public PrecioResponse calcularPrecioEntrada(@RequestParam int funcionId,
                                                @RequestParam(required = false) Integer espectadorId,
                                                @RequestParam double precioBase) {
        double total = precioService.calcularPrecioEntrada(funcionId, espectadorId, precioBase);
        return new PrecioResponse(total);
    }

    @GetMapping("/total")
    public PrecioResponse calcularTotal(@RequestParam int funcionId,
                                        @RequestParam(required = false) Integer espectadorId,
                                        @RequestParam double precioBase,
                                        @RequestParam int cantidadEntradas) {
        double total = precioService.calcularTotal(funcionId, espectadorId, precioBase, cantidadEntradas);
        return new PrecioResponse(total);
    }

    public record PrecioResponse(double total) {
    }
}
