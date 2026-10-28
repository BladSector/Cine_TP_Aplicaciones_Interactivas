package service.ventas;

import modelo.entidades.Espectador;
import modelo.entidades.Membresia;
import modelo.entidades.MetodoDePago;
import modelo.entidades.Suscripcion;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
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
    private static final int DIAS_PARA_MEJORA = 7;
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

    @Transactional
    public Suscripcion buscarVigente(int espectadorId) {
        return resolverVigente(suscripcionRepository.bloquearActivas(espectadorId));
    }

    @Transactional
    public Cotizacion cotizar(int espectadorId, int membresiaId) {
        Membresia membresia = buscarMembresia(membresiaId);
        Suscripcion vigente = resolverVigente(suscripcionRepository.bloquearActivas(espectadorId));
        return crearCotizacion(vigente, membresia);
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

        Membresia membresia = buscarMembresia(membresiaId);
        MetodoDePago metodoDePago = metodoDePagoService.validarParaCompra(metodoDePagoId, espectador);
        Suscripcion vigente = resolverVigente(suscripcionRepository.bloquearActivas(espectadorId));
        Cotizacion cotizacion = crearCotizacion(vigente, membresia);

        if (cotizacion.tipo() == TipoOperacion.CAMBIO_PROGRAMADO) {
            vigente.programarCambio(membresia, metodoDePago);
            return suscripcionRepository.save(vigente);
        }

        LocalDate inicio = LocalDate.now();
        LocalDate fin = inicio.plusMonths(1);
        if (cotizacion.tipo() == TipoOperacion.MEJORA_INMEDIATA_DIFERENCIA) {
            vigente.desactivar();
            inicio = vigente.getFechaInicio();
            fin = vigente.getFechaFin();
        } else if (cotizacion.tipo() == TipoOperacion.MEJORA_INMEDIATA_COMPLETA) {
            vigente.desactivar();
        }

        Suscripcion suscripcion = suscripcionRepository.save(new Suscripcion(
                espectador,
                membresia,
                metodoDePago,
                cotizacion.importeAhora(),
                inicio,
                fin
        ));
        eventPublisher.publishEvent(new MembresiaAdquiridaEvent(suscripcion.getId()));
        return suscripcion;
    }

    @Transactional
    public void darDeBaja(int espectadorId) {
        Suscripcion vigente = resolverVigente(suscripcionRepository.bloquearActivas(espectadorId));
        if (vigente == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tenés una membresía activa.");
        }
        vigente.desactivar();
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void aplicarCambiosProgramados() {
        List<Suscripcion> vencidas = suscripcionRepository
                .findByActivaTrueAndFechaFinBeforeAndMembresiaProgramadaIsNotNull(LocalDate.now());
        for (Suscripcion suscripcion : vencidas) {
            activarCambioProgramado(suscripcion);
        }
    }

    private Membresia buscarMembresia(int membresiaId) {
        Membresia membresia = membresiaRepository.findById(membresiaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una membresía con ese id."
                ));
        if (membresia.getPrecioMensual() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La membresía no tiene un precio válido."
            );
        }
        return membresia;
    }

    private Cotizacion crearCotizacion(Suscripcion vigente, Membresia nuevaMembresia) {
        LocalDate hoy = LocalDate.now();
        if (vigente == null) {
            return new Cotizacion(
                    TipoOperacion.NUEVA,
                    nuevaMembresia.getPrecioMensual(),
                    nuevaMembresia.getPrecioMensual(),
                    hoy,
                    hoy.plusMonths(1),
                    "La membresía comienza hoy y tendrá vigencia durante un mes."
            );
        }

        if (vigente.getMembresia().getId() == nuevaMembresia.getId()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya tenés la membresía " + vigente.getMembresia().getNombre()
                            + " activa hasta el " + FECHA.format(vigente.getFechaFin()) + "."
            );
        }

        boolean esMejora = nuevaMembresia.getPrecioMensual()
                > vigente.getMembresia().getPrecioMensual();
        boolean dentroDelPlazo = !hoy.isAfter(vigente.getFechaInicio().plusDays(DIAS_PARA_MEJORA));
        if (esMejora && dentroDelPlazo) {
            double diferencia = nuevaMembresia.getPrecioMensual()
                    - vigente.getMembresia().getPrecioMensual();
            return new Cotizacion(
                    TipoOperacion.MEJORA_INMEDIATA_DIFERENCIA,
                    diferencia,
                    nuevaMembresia.getPrecioMensual(),
                    vigente.getFechaInicio(),
                    vigente.getFechaFin(),
                    "Pagás únicamente la diferencia y conservás el vencimiento actual."
            );
        }

        if (esMejora) {
            return new Cotizacion(
                    TipoOperacion.MEJORA_INMEDIATA_COMPLETA,
                    nuevaMembresia.getPrecioMensual(),
                    nuevaMembresia.getPrecioMensual(),
                    hoy,
                    hoy.plusMonths(1),
                    "Ya pasaron los 7 días para pagar solo la diferencia. "
                            + "Se cobrará el precio completo y el plan superior se activará ahora."
            );
        }

        LocalDate inicioProgramado = vigente.getFechaFin().plusDays(1);
        return new Cotizacion(
                TipoOperacion.CAMBIO_PROGRAMADO,
                0,
                nuevaMembresia.getPrecioMensual(),
                inicioProgramado,
                inicioProgramado.plusMonths(1),
                "Los cambios a un plan de menor precio no generan reintegros. "
                        + "El cambio se aplicará cuando termine tu plan actual."
        );
    }

    private Suscripcion resolverVigente(List<Suscripcion> activas) {
        if (activas.isEmpty()) {
            return null;
        }

        Suscripcion actual = activas.get(0);
        for (int i = 1; i < activas.size(); i++) {
            activas.get(i).desactivar();
        }
        if (estaVigente(actual)) {
            return actual;
        }
        if (actual.tieneCambioProgramado()) {
            return activarCambioProgramado(actual);
        }
        actual.desactivar();
        return null;
    }

    private Suscripcion activarCambioProgramado(Suscripcion anterior) {
        anterior.desactivar();
        LocalDate inicioPrevisto = anterior.getFechaFin().plusDays(1);
        LocalDate inicio = inicioPrevisto.isBefore(LocalDate.now()) ? LocalDate.now() : inicioPrevisto;
        Suscripcion nueva = suscripcionRepository.save(new Suscripcion(
                anterior.getEspectador(),
                anterior.getMembresiaProgramada(),
                anterior.getMetodoPagoProgramado(),
                anterior.getMembresiaProgramada().getPrecioMensual(),
                inicio,
                inicio.plusMonths(1)
        ));
        eventPublisher.publishEvent(new MembresiaAdquiridaEvent(nueva.getId()));
        return nueva;
    }

    private boolean estaVigente(Suscripcion suscripcion) {
        return suscripcion.isActiva()
                && suscripcion.getFechaFin() != null
                && !suscripcion.getFechaFin().isBefore(LocalDate.now());
    }

    public enum TipoOperacion {
        NUEVA,
        MEJORA_INMEDIATA_DIFERENCIA,
        MEJORA_INMEDIATA_COMPLETA,
        CAMBIO_PROGRAMADO
    }

    public record Cotizacion(TipoOperacion tipo, double importeAhora, double importeRenovacion,
                             LocalDate fechaInicio, LocalDate fechaFin, String mensaje) {
    }
}
