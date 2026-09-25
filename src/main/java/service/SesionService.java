package service;

import jakarta.servlet.http.HttpSession;
import modelo.Permiso;
import modelo.RolEmpleado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SesionService {
    public static final String ESPECTADOR_ID = "espectadorId";
    public static final String ADMIN = "admin";
    public static final String EMPLEADO_ID = "empleadoId";
    public static final String ROL_EMPLEADO = "rolEmpleado";
    private final PermisoService permisoService;

    public SesionService(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    public void iniciarEspectador(HttpSession sesion, int espectadorId) {
        sesion.setAttribute(ESPECTADOR_ID, espectadorId);
        sesion.removeAttribute(ADMIN);
        sesion.removeAttribute(EMPLEADO_ID);
        sesion.removeAttribute(ROL_EMPLEADO);
    }

    public void iniciarAdministrador(HttpSession sesion) {
        sesion.setAttribute(ADMIN, true);
        sesion.removeAttribute(ESPECTADOR_ID);
        sesion.removeAttribute(EMPLEADO_ID);
        sesion.removeAttribute(ROL_EMPLEADO);
    }

    public void iniciarEmpleado(HttpSession sesion, int empleadoId, RolEmpleado rol) {
        sesion.setAttribute(EMPLEADO_ID, empleadoId);
        sesion.setAttribute(ROL_EMPLEADO, rol);
        sesion.removeAttribute(ESPECTADOR_ID);
        sesion.removeAttribute(ADMIN);
    }

    public boolean esAdministrador(HttpSession sesion) {
        return esDuenio(sesion);
    }

    public boolean esDuenio(HttpSession sesion) {
        return sesion != null && Boolean.TRUE.equals(sesion.getAttribute(ADMIN));
    }

    public boolean esEmpleado(HttpSession sesion) {
        RolEmpleado rol = obtenerRolEmpleado(sesion);
        return rol == RolEmpleado.SUPERVISOR
                || rol == RolEmpleado.STAFF
                || rol == RolEmpleado.TECNICO
                || rol == RolEmpleado.EMPLEADO;
    }

    public boolean esPersonal(HttpSession sesion) {
        return esDuenio(sesion) || esEmpleado(sesion);
    }

    public Integer obtenerEspectadorId(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ESPECTADOR_ID);
        return valor instanceof Integer id ? id : null;
    }

    public Integer obtenerEmpleadoId(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(EMPLEADO_ID);
        return valor instanceof Integer id ? id : null;
    }

    public void validarEspectador(HttpSession sesion, int espectadorId) {
        Integer idSesion = obtenerEspectadorId(sesion);
        if (idSesion == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesion.");
        }
        if (idSesion != espectadorId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede operar sobre la cuenta de otro espectador.");
        }
    }

    public void validarAdministrador(HttpSession sesion) {
        validarDuenio(sesion);
    }

    public void validarDuenio(HttpSession sesion) {
        if (!esDuenio(sesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere una sesion de administrador.");
        }
    }

    public void validarPersonal(HttpSession sesion) {
        if (!esPersonal(sesion)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere una sesion del personal del cine.");
        }
    }

    public boolean tienePermiso(HttpSession sesion, Permiso permiso) {
        return esDuenio(sesion) || permisoService.tienePermiso(obtenerRolEmpleado(sesion), permiso);
    }

    public void validarPermiso(HttpSession sesion, Permiso permiso) {
        if (!tienePermiso(sesion, permiso)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no tiene permiso para realizar esta operacion."
            );
        }
    }

    public RolEmpleado obtenerRolEmpleado(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Object valor = sesion.getAttribute(ROL_EMPLEADO);
        return valor instanceof RolEmpleado rol ? rol : null;
    }

    public void cerrar(HttpSession sesion) {
        sesion.invalidate();
    }
}
