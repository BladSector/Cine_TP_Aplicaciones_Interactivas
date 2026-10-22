package service.ventas;

import modelo.entidades.Espectador;
import modelo.entidades.Membresia;
import modelo.entidades.MetodoDePago;
import modelo.entidades.Suscripcion;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.usuarios.EspectadorRepository;
import repository.ventas.MembresiaRepository;
import repository.ventas.SuscripcionRepository;
import service.comunicacion.MembresiaAdquiridaEvent;
import service.usuarios.MetodoDePagoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MembresiaService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MembresiaRepository membresiaRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final EspectadorRepository espectadorRepository;
    private final MetodoDePagoService metodoDePagoService;
    private final ApplicationEventPublisher eventPublisher;

    public MembresiaService(MembresiaRepository membresiaRepository,
                            SuscripcionRepository suscripcionRepository,
                            EspectadorRepository espectadorRepository,
                            MetodoDePagoService metodoDePagoService,
                            ApplicationEventPublisher eventPublisher) {
        this.membresiaRepository = membresiaRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.espectadorRepository = espectadorRepository;
        this.metodoDePagoService = metodoDePagoService;
        this.eventPublisher = eventPublisher;
    }

    public List<Membresia> listar() {
        return membresiaRepository.findAllByOrderByPrecioMensualAsc();
    }

    public Suscripcion buscarVigente(int espectadorId) {
        return suscripcionRepository.findFirstByEspectadorIdAndActivaTrueOrderByFechaFinDesc(espectadorId)
                .filter(this::estaVigente)
                .orElse(null);
    }

    @Transactional
    public Suscripcion comprar(int espectadorId, int metodoDePagoId, int membresiaId) {
        Espectador espectador = espectadorRepository.findById(espectadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un espectador con ese id."
                ));
        if (!espectador.isEmailVerificado()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe verificar el email antes de comprar."
            );
        }

        Membresia membresia = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una membresia con ese id."
                ));
        if (membresia.getPrecioMensual() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La membresia no tiene un precio valido."
            );
        }

        for (Suscripcion activa : suscripcionRepository.bloquearActivas(espectadorId)) {
            if (estaVigente(activa) && activa.getMembresia().getId() == membresia.getId()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ya tenés la membresía " + activa.getMembresia().getNombre()
                                + " activa hasta el " + FECHA.format(activa.getFechaFin()) + "."
                );
            }
            activa.desactivar();
        }

        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);
        LocalDate inicio = LocalDate.now();
        Suscripcion suscripcion = suscripcionRepository.save(new Suscripcion(
                espectador,
                membresia,
                metodoDePago,
                membresia.getPrecioMensual(),
                inicio,
                inicio.plusMonths(1)
        ));
        eventPublisher.publishEvent(new MembresiaAdquiridaEvent(suscripcion.getId()));
        return suscripcion;
    }

    @Transactional
    public void darDeBaja(int espectadorId) {
        boolean habiaVigente = false;
        for (Suscripcion activa : suscripcionRepository.bloquearActivas(espectadorId)) {
            if (estaVigente(activa)) {
                habiaVigente = true;
            }
            activa.desactivar();
        }
        if (!habiaVigente) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tenés una membresía activa.");
        }
    }

    private boolean estaVigente(Suscripcion suscripcion) {
        return suscripcion.isActiva()
                && suscripcion.getFechaFin() != null
                && !suscripcion.getFechaFin().isBefore(LocalDate.now());
    }
}
