package service;

import modelo.CalculadoraPrecio;
import modelo.Espectador;
import modelo.Funcion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EspectadorRepository;
import repository.FuncionRepository;

@Service
public class PrecioService {
    private final FuncionRepository funcionRepository;
    private final EspectadorRepository espectadorRepository;
    private final CalculadoraPrecio calculadoraPrecio;

    public PrecioService(FuncionRepository funcionRepository, EspectadorRepository espectadorRepository) {
        this.funcionRepository = funcionRepository;
        this.espectadorRepository = espectadorRepository;
        this.calculadoraPrecio = new CalculadoraPrecio();
    }

    public double calcularPrecioEntrada(int funcionId, Integer espectadorId, double precioBase) {
        try {
            Funcion funcion = buscarFuncion(funcionId);
            Espectador espectador = buscarEspectadorOpcional(espectadorId);
            return calculadoraPrecio.calcularPrecioEntrada(funcion, espectador, precioBase);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public double calcularTotal(int funcionId, Integer espectadorId, double precioBase, int cantidadEntradas) {
        try {
            Funcion funcion = buscarFuncion(funcionId);
            Espectador espectador = buscarEspectadorOpcional(espectadorId);
            return calculadoraPrecio.calcularTotal(funcion, espectador, precioBase, cantidadEntradas);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private Funcion buscarFuncion(int id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una funcion con ese id."));
    }

    private Espectador buscarEspectadorOpcional(Integer id) {
        if (id == null) {
            return null;
        }

        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
    }
}
