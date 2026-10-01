package com.tp.cine.seguridad;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import service.administracion.AuditoriaService;

import java.util.Set;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {
    public static final String DETALLE_PERSONALIZADO_REQUEST =
            AuditoriaInterceptor.class.getName() + ".detallePersonalizado";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditoriaInterceptor.class);
    private static final String ACTOR_REQUEST = AuditoriaInterceptor.class.getName() + ".actor";
    private static final Set<String> METODOS_ESCRITURA = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditoriaService auditoriaService;

    public AuditoriaInterceptor(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!debeAuditar(request.getMethod(), rutaSinContexto(request))) {
            return true;
        }
        AuditoriaService.Actor actor = auditoriaService.obtenerActor(request.getSession(false));
        if (actor != null) {
            request.setAttribute(ACTOR_REQUEST, actor);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String ruta = rutaSinContexto(request);
        if (!debeAuditar(request.getMethod(), ruta)) {
            return;
        }

        AuditoriaService.Actor actor = (AuditoriaService.Actor) request.getAttribute(ACTOR_REQUEST);
        if (actor == null) {
            actor = auditoriaService.obtenerActor(request.getSession(false));
        }
        if (actor == null) {
            return;
        }

        String resultado = response.getStatus() < 400 && ex == null ? "EXITOSA" : "FALLIDA";
        // Algunos casos agregan un detalle de negocio legible en lugar de la ruta HTTP.
        Object detallePersonalizado = request.getAttribute(DETALLE_PERSONALIZADO_REQUEST);
        String detalle = detallePersonalizado instanceof String texto && !texto.isBlank()
                ? texto
                : request.getMethod() + " " + ruta + " - HTTP " + response.getStatus();
        try {
            auditoriaService.registrar(
                    actor,
                    resolverAccion(request.getMethod(), ruta),
                    resolverEntidad(ruta),
                    resolverEntidadId(ruta),
                    detalle,
                    resultado
            );
        } catch (RuntimeException errorAuditoria) {
            LOGGER.error("No se pudo registrar la acción de auditoría.", errorAuditoria);
        }
    }

    private boolean debeAuditar(String metodo, String ruta) {
        if (ruta.matches("/avisos-sala/\\d+/leer")) {
            return false;
        }
        return METODOS_ESCRITURA.contains(metodo)
                || ("POST".equals(metodo) && ruta.matches("/sesion/(personal|admin|empleado)"));
    }

    private String resolverAccion(String metodo, String ruta) {
        if ("POST".equals(metodo) && ruta.matches("/sesion/(personal|admin|empleado)")) {
            return "INICIAR_SESION";
        }
        if ("DELETE".equals(metodo) && "/sesion".equals(ruta)) {
            return "CERRAR_SESION";
        }
        if (ruta.matches("/empleados/\\d+/contrasenia")) {
            return "RESTABLECER_CONTRASENA";
        }
        if (ruta.matches("/empleados/\\d+/activar")) {
            return "ACTIVAR_EMPLEADO";
        }
        if (ruta.matches("/empleados/\\d+/desactivar")) {
            return "DESACTIVAR_EMPLEADO";
        }
        if (ruta.matches("/funciones/\\d+/horario")) {
            return "MODIFICAR_HORARIO";
        }
        if (ruta.matches("/salas/\\d+/estado")) {
            return "CAMBIAR_ESTADO_SALA";
        }
        if (ruta.matches("/salas/\\d+/(matriz|distribucion)")) {
            return "MODIFICAR_DISTRIBUCION_SALA";
        }
        if (ruta.matches("/butacas/\\d+/ocupar")) {
            return "OCUPAR_BUTACA";
        }
        if (ruta.matches("/butacas/\\d+/liberar")) {
            return "LIBERAR_BUTACA";
        }
        if (ruta.matches("/butacas/\\d+/fuera-de-servicio")) {
            return "MARCAR_BUTACA_FUERA_DE_SERVICIO";
        }
        if (ruta.matches("/tickets/\\d+/validar-entradas")
                || ruta.matches("/entradas/\\d+/escanear")) {
            return "VALIDAR_ENTRADAS";
        }
        if (ruta.matches("/tickets/\\d+/entregar-consumos")
                || ruta.matches("/items-consumo/\\d+/entregar")) {
            return "ENTREGAR_CONSUMOS";
        }
        if (ruta.matches("/tickets/\\d+/procesar-ingreso")) {
            return "PROCESAR_INGRESO";
        }
        if (ruta.matches("/entradas/\\d+/reembolsar")) {
            return "REEMBOLSAR_ENTRADA";
        }
        if (ruta.matches("/tickets/\\d+/reembolsar")) {
            return "REEMBOLSAR_TICKET";
        }
        return switch (metodo) {
            case "POST" -> "CREAR";
            case "PUT", "PATCH" -> "ACTUALIZAR";
            case "DELETE" -> "ELIMINAR";
            default -> metodo;
        };
    }

    private String resolverEntidad(String ruta) {
        String primerSegmento = ruta.replaceFirst("^/", "").split("/")[0];
        return switch (primerSegmento) {
            case "categorias" -> "CATEGORIA";
            case "peliculas" -> "PELICULA";
            case "salas" -> "SALA";
            case "funciones" -> "FUNCION";
            case "butacas" -> "BUTACA";
            case "productos-confiteria" -> "PRODUCTO_CONFITERIA";
            case "entradas" -> "ENTRADA";
            case "tickets" -> "TICKET";
            case "items-consumo" -> "CONSUMO";
            case "empleados" -> "EMPLEADO";
            case "sesion" -> "SESION";
            case "compras" -> "COMPRA";
            default -> primerSegmento.isBlank() ? "SISTEMA" : primerSegmento.toUpperCase();
        };
    }

    private Integer resolverEntidadId(String ruta) {
        for (String segmento : ruta.split("/")) {
            if (segmento.matches("\\d+")) {
                try {
                    return Integer.valueOf(segmento);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String rutaSinContexto(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        String contexto = request.getContextPath();
        return contexto.isEmpty() ? ruta : ruta.substring(contexto.length());
    }
}
