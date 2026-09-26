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
        permisosPorRol.put(RolEmpleado.EMPLEADO, EnumSet.of(
                Permiso.VER_REPORTES,
                Permiso.GESTIONAR_CARTELERA,
                Permiso.OPERAR_POS,
                Permiso.VALIDAR_TICKETS,
                Permiso.SOLICITAR_REEMBOLSO,
                Permiso.AUTORIZAR_REEMBOLSO,
                Permiso.CONTROLAR_INVENTARIO,
                Permiso.GESTIONAR_SALAS,
                Permiso.COMPLETAR_MANTENIMIENTO,
                Permiso.VER_CRONOGRAMA
        ));
    }

    public boolean tienePermiso(RolEmpleado rol, Permiso permiso) {
        return rol != null && permisosPorRol.getOrDefault(rol, Set.of()).contains(permiso);
    }
}
