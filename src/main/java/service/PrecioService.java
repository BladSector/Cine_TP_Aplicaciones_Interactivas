package service;

import modelo.EstadoEntrada;
import modelo.FormatoFuncion;
import modelo.Espectador;
import modelo.Funcion;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EspectadorRepository;
import repository.EntradaRepository;
import repository.FuncionRepository;

import java.time.DayOfWeek;
import java.util.List;

@Service
public class PrecioService {
    private static final double RECARGO_3D = 0.20;
    private static final double DESCUENTO_FIDELIDAD = 0.10;
    private static final int ENTRADAS_PARA_CLIENTE_FRECUENTE = 5;

    private final FuncionRepository funcionRepository;
    private final EspectadorRepository espectadorRepository;
    private final EntradaRepository entradaRepository;

    public PrecioService(FuncionRepository funcionRepository, EspectadorRepository espectadorRepository,
                         EntradaRepository entradaRepository) {
        this.funcionRepository = funcionRepository;
        this.espectadorRepository = espectadorRepository;
        this.entradaRepository = entradaRepository;
    }

    public double calcularPrecioEntrada(int funcionId, Integer espectadorId, double precioBase) {
        Funcion funcion = buscarFuncion(funcionId);
        Espectador espectador = buscarEspectadorOpcional(espectadorId);
        return calcularPrecioUnitario(funcion, espectador, precioBase);
    }

    public double calcularTotal(int funcionId, Integer espectadorId, double precioBase, int cantidadEntradas) {
        Funcion funcion = buscarFuncion(funcionId);
        Espectador espectador = buscarEspectadorOpcional(espectadorId);
        return calcularTotal(funcion, espectador, precioBase, cantidadEntradas);
    }

    public double calcularTotal(Funcion funcion, Espectador espectador, double precioBase, int cantidadEntradas) {
        validarDatos(funcion, precioBase, cantidadEntradas);
        double precioUnitario = calcularPrecioUnitario(funcion, espectador, precioBase);
        int entradasACobrar = funcion.getFecha().getDayOfWeek() == DayOfWeek.WEDNESDAY
                ? (cantidadEntradas / 2) + (cantidadEntradas % 2)
                : cantidadEntradas;
        return precioUnitario * entradasACobrar;
    }

    public double calcularPrecioUnitario(Funcion funcion, Espectador espectador, double precioBase) {
        validarDatos(funcion, precioBase, 1);
        double precio = precioBase;

        if (funcion.getFormato() == FormatoFuncion.TRES_D) {
            precio += precioBase * RECARGO_3D;
        }
        if (esClienteFrecuente(espectador)) {
            precio -= precio * DESCUENTO_FIDELIDAD;
        }
        return precio;
    }

    private boolean esClienteFrecuente(Espectador espectador) {
        if (espectador == null) {
            return false;
        }
        long entradasValidas = entradaRepository.countByEspectadorIdAndEstadoIn(
                espectador.getId(),
                List.of(EstadoEntrada.PAGADA, EstadoEntrada.ESCANEADA)
        );
        return entradasValidas >= ENTRADAS_PARA_CLIENTE_FRECUENTE;
    }

    private void validarDatos(Funcion funcion, double precioBase, int cantidadEntradas) {
        if (funcion == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La funcion es obligatoria.");
        }
        if (precioBase <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio base debe ser mayor a 0.");
        }
        if (cantidadEntradas <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad de entradas debe ser mayor a 0.");
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
