package service.ventas;

import modelo.entidades.Butaca;
import modelo.entidades.Espectador;
import modelo.entidades.Funcion;
import modelo.entidades.ReservaButaca;
import modelo.enums.EstadoEntrada;
import modelo.enums.EstadoSala;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.cartelera.FuncionRepository;
import repository.instalaciones.ButacaRepository;
import repository.usuarios.EspectadorRepository;
import repository.ventas.EntradaRepository;
import repository.ventas.ReservaButacaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaButacaService {
    private static final int MINUTOS_RESERVA = 10;

    private final ReservaButacaRepository reservaRepository;
    private final ButacaRepository butacaRepository;
    private final FuncionRepository funcionRepository;
    private final EspectadorRepository espectadorRepository;
    private final EntradaRepository entradaRepository;

    public ReservaButacaService(ReservaButacaRepository reservaRepository,
                                ButacaRepository butacaRepository,
                                FuncionRepository funcionRepository,
                                EspectadorRepository espectadorRepository,
                                EntradaRepository entradaRepository) {
        this.reservaRepository = reservaRepository;
        this.butacaRepository = butacaRepository;
        this.funcionRepository = funcionRepository;
        this.espectadorRepository = espectadorRepository;
        this.entradaRepository = entradaRepository;
    }

    @Transactional
    public ReservaButaca reservar(int funcionId, int butacaId, int espectadorId) {
        Funcion funcion = buscarFuncion(funcionId);
        Espectador espectador = buscarEspectador(espectadorId);
        Butaca butaca = butacaRepository.buscarPorIdConBloqueo(butacaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la butaca."));

        validarReserva(funcion, butaca);
        LocalDateTime ahora = LocalDateTime.now();
        reservaRepository.deleteByVenceEnLessThanEqual(ahora);
        ReservaButaca existente = reservaRepository.findByFuncionIdAndButacaId(funcionId, butacaId)
                .orElse(null);

        if (existente != null) {
            if (existente.getEspectador().getId() != espectadorId) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La butaca acaba de ser reservada por otro espectador."
                );
            }
            return existente;
        }

        LocalDateTime vencimientoCompra = reservaRepository.findByFuncionIdOrderByButacaId(funcionId).stream()
                .filter(reserva -> reserva.getEspectador().getId() == espectadorId)
                .map(ReservaButaca::getVenceEn)
                .min(LocalDateTime::compareTo)
                .orElse(ahora.plusMinutes(MINUTOS_RESERVA));

        try {
            return reservaRepository.saveAndFlush(new ReservaButaca(
                    funcion,
                    butaca,
                    espectador,
                    vencimientoCompra
            ));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La butaca acaba de ser reservada por otro espectador."
            );
        }
    }

    @Transactional
    public List<ReservaButaca> listarActivas(int funcionId) {
        reservaRepository.deleteByVenceEnLessThanEqual(LocalDateTime.now());
        return reservaRepository.findByFuncionIdOrderByButacaId(funcionId);
    }

    @Transactional
    public void liberar(int funcionId, int butacaId, int espectadorId) {
        butacaRepository.buscarPorIdConBloqueo(butacaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la butaca."));
        ReservaButaca reserva = reservaRepository.findByFuncionIdAndButacaId(funcionId, butacaId)
                .orElse(null);
        if (reserva == null || reserva.estaVencida(LocalDateTime.now())) {
            if (reserva != null) reservaRepository.delete(reserva);
            return;
        }
        if (reserva.getEspectador().getId() != espectadorId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La reserva pertenece a otro espectador.");
        }
        reservaRepository.delete(reserva);
    }

    @Transactional
    public void validarDisponibilidadParaCompra(Funcion funcion, Espectador espectador, List<Butaca> butacas) {
        LocalDateTime ahora = LocalDateTime.now();
        for (Butaca butaca : butacas) {
            ReservaButaca reserva = reservaRepository
                    .findByFuncionIdAndButacaId(funcion.getId(), butaca.getId())
                    .orElse(null);
            if (reserva == null || reserva.estaVencida(ahora)) {
                if (reserva != null) reservaRepository.delete(reserva);
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La reserva de una de las butacas venció. Debe seleccionarlas nuevamente."
                );
            }
            if (reserva.getEspectador().getId() != espectador.getId()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Una de las butacas está reservada temporalmente por otro espectador."
                );
            }
        }
    }

    @Transactional
    public void liberarTrasCompra(Funcion funcion, Espectador espectador, List<Butaca> butacas) {
        for (Butaca butaca : butacas) {
            reservaRepository.findByFuncionIdAndButacaId(funcion.getId(), butaca.getId())
                    .filter(reserva -> reserva.getEspectador().getId() == espectador.getId())
                    .ifPresent(reservaRepository::delete);
        }
    }

    private void validarReserva(Funcion funcion, Butaca butaca) {
        if (funcion.getSala().getEstado() != EstadoSala.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sala no está disponible.");
        }
        if (!LocalDateTime.of(funcion.getFecha(), funcion.getHorario()).isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La función ya comenzó.");
        }
        if (butaca.getSala() == null || butaca.getSala().getId() != funcion.getSala().getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La butaca no pertenece a la función.");
        }
        if (!butaca.estaDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La butaca no está habilitada.");
        }
        if (entradaRepository.existeEntradaActivaParaButaca(
                funcion.getId(), butaca.getId(), List.of(EstadoEntrada.REEMBOLSADA, EstadoEntrada.CANCELADA))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La butaca ya fue vendida para esta función.");
        }
    }

    private Funcion buscarFuncion(int id) {
        return funcionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la función."));
    }

    private Espectador buscarEspectador(int id) {
        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el espectador."));
    }
}
