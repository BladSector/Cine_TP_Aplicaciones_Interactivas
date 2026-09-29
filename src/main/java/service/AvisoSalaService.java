package service;

import modelo.AvisoSala;
import modelo.Empleado;
import modelo.EstadoSala;
import modelo.Sala;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import repository.AvisoSalaRepository;
import repository.EmpleadoRepository;

import java.util.List;

@Service
public class AvisoSalaService {
    private final AvisoSalaRepository avisoSalaRepository;
    private final EmpleadoRepository empleadoRepository;

    public AvisoSalaService(AvisoSalaRepository avisoSalaRepository,
                            EmpleadoRepository empleadoRepository) {
        this.avisoSalaRepository = avisoSalaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public void notificarCambio(Sala sala, EstadoSala estado, String descripcion,
                                Integer creadoPorEmpleadoId, Integer destinatarioEmpleadoId) {
        String detalle = normalizarDescripcion(estado, descripcion);
        String usuarioOrigen = creadoPorEmpleadoId == null
                ? "admin"
                : empleadoRepository.findById(creadoPorEmpleadoId)
                        .map(Empleado::getUsuario)
                        .orElse("empleado-" + creadoPorEmpleadoId);

        List<Empleado> destinatarios;
        if (destinatarioEmpleadoId != null) {
            if (destinatarioEmpleadoId.equals(creadoPorEmpleadoId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El aviso debe enviarse a otro empleado."
                );
            }
            Empleado destinatario = empleadoRepository.findById(destinatarioEmpleadoId)
                    .filter(Empleado::isActivo)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El empleado destinatario no existe o está inactivo."
                    ));
            destinatarios = List.of(destinatario);
        } else {
            destinatarios = empleadoRepository.findByActivoTrueOrderByNombreAscApellidoAsc().stream()
                    .filter(empleado -> !Integer.valueOf(empleado.getId()).equals(creadoPorEmpleadoId))
                    .toList();
        }

        List<AvisoSala> avisos = destinatarios.stream()
                .map(destinatario -> new AvisoSala(
                        sala,
                        estado,
                        detalle,
                        creadoPorEmpleadoId,
                        usuarioOrigen,
                        destinatario.getId(),
                        destinatario.getUsuario()
                ))
                .toList();
        avisoSalaRepository.saveAll(avisos);
    }

    public List<AvisoSala> listarPendientes(int empleadoId) {
        return avisoSalaRepository
                .findByDestinatarioEmpleadoIdAndLeidoFalseOrderByFechaHoraAsc(empleadoId);
    }

    public List<AvisoSala> listarHistorial(int empleadoId) {
        return avisoSalaRepository.findByDestinatarioEmpleadoIdOrderByFechaHoraDesc(empleadoId);
    }

    @Transactional
    public AvisoSala marcarLeido(long avisoId, int empleadoId) {
        AvisoSala aviso = avisoSalaRepository.findById(avisoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un aviso con ese id."
                ));
        if (aviso.getDestinatarioEmpleadoId() != empleadoId) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El aviso pertenece a otro empleado."
            );
        }
        aviso.marcarLeido();
        return avisoSalaRepository.save(aviso);
    }

    private String normalizarDescripcion(EstadoSala estado, String descripcion) {
        String detalle = descripcion == null ? "" : descripcion.trim();
        if (estado == EstadoSala.PROBLEMA_PARTICULAR && detalle.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe describir el problema particular de la sala."
            );
        }
        return detalle.isBlank() ? "La sala cambió al estado " + estado.name() + "." : detalle;
    }
}
