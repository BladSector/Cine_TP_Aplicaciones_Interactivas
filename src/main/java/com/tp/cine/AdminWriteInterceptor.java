package com.tp.cine;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Permiso;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import service.SesionService;

import java.util.Set;

@Component
public class AdminWriteInterceptor implements HandlerInterceptor {
    private static final Set<String> METODOS_DE_ESCRITURA = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final SesionService sesionService;

    public AdminWriteInterceptor(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (METODOS_DE_ESCRITURA.contains(request.getMethod())) {
            var sesion = request.getSession(false);
            if (sesionService.esDuenio(sesion)) {
                return true;
            }
            Permiso permisoRequerido = permisoRequerido(request);
            if (permisoRequerido != null && sesionService.tienePermiso(sesion, permisoRequerido)) {
                return true;
            }
            sesionService.validarDuenio(sesion);
        }
        return true;
    }

    private Permiso permisoRequerido(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        if ("PUT".equals(request.getMethod()) && ruta.matches("/funciones/\\d+/horario")) {
            return Permiso.GESTIONAR_CARTELERA;
        }
        if ("PUT".equals(request.getMethod()) && (ruta.matches("/salas/\\d+/estado")
                || ruta.matches("/butacas/\\d+/(ocupar|liberar|fuera-de-servicio)"))) {
            return Permiso.GESTIONAR_SALAS;
        }
        if ("PUT".equals(request.getMethod()) && (ruta.matches("/entradas/\\d+/escanear")
                || ruta.matches("/tickets/\\d+/validar-entradas"))) {
            return Permiso.VALIDAR_TICKETS;
        }
        if ("PUT".equals(request.getMethod()) && (ruta.matches("/items-consumo/\\d+/entregar")
                || ruta.matches("/tickets/\\d+/entregar-consumos"))) {
            return Permiso.OPERAR_POS;
        }
        if ("PUT".equals(request.getMethod()) && ruta.matches("/entradas/\\d+/reembolsar")) {
            return Permiso.AUTORIZAR_REEMBOLSO;
        }
        return null;
    }
}
