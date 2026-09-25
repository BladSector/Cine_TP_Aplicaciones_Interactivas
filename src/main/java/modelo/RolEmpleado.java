package modelo;

public enum RolEmpleado {
    SUPERVISOR,
    STAFF,
    TECNICO,

    // Valores heredados para poder leer registros creados antes de la ACL.
    DUENIO,
    EMPLEADO
}
