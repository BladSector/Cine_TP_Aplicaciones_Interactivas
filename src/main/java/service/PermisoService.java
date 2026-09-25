package service;

import modelo.Permiso;
import modelo.RolEmpleado;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class PermisoService {
    private final Map<RolEmpleado, Set<Permiso>> permisosPorRol = new EnumMap<>(RolEmpleado.class);

    public PermisoService() {
        permisosPorRol.put(RolEmpleado.SUPERVISOR, EnumSet.of(
                Permiso.VER_REPORTES,
                Permiso.GESTIONAR_CARTELERA,
                Permiso.OPERAR_POS,
                Permiso.VALIDAR_TICKETS,
                Permiso.SOLICITAR_REEMBOLSO,
                Permiso.AUTORIZAR_REEMBOLSO,
                Permiso.CONTROLAR_INVENTARIO,
                Permiso.GESTIONAR_SALAS,
                Permiso.VER_CRONOGRAMA
        ));
        permisosPorRol.put(RolEmpleado.STAFF, EnumSet.of(
                Permiso.OPERAR_POS,
                Permiso.VALIDAR_TICKETS,
                Permiso.SOLICITAR_REEMBOLSO
        ));
        permisosPorRol.put(RolEmpleado.TECNICO, EnumSet.of(
                Permiso.GESTIONAR_SALAS,
                Permiso.COMPLETAR_MANTENIMIENTO,
                Permiso.VER_CRONOGRAMA
        ));

        // Los registros antiguos de EMPLEADO conservan las funciones de Staff.
        permisosPorRol.put(RolEmpleado.EMPLEADO, permisosPorRol.get(RolEmpleado.STAFF));
        permisosPorRol.put(RolEmpleado.DUENIO, EnumSet.noneOf(Permiso.class));
    }

    public boolean tienePermiso(RolEmpleado rol, Permiso permiso) {
        return rol != null && permisosPorRol.getOrDefault(rol, Set.of()).contains(permiso);
    }
}
