package controller.usuarios;

import jakarta.servlet.http.HttpSession;
import modelo.entidades.Empleado;
import modelo.enums.RolEmpleado;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.usuarios.EmpleadoService;
import service.usuarios.SesionService;

import java.util.List;

@RestController
@RequestMapping("/empleados")
public class EmpleadoController {
    private final EmpleadoService empleadoService;
    private final SesionService sesionService;

    public EmpleadoController(EmpleadoService empleadoService, SesionService sesionService) {
        this.empleadoService = empleadoService;
        this.sesionService = sesionService;
    }

    @GetMapping
    public List<EmpleadoResponse> listar(HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return empleadoService.listar().stream().map(EmpleadoResponse::desde).toList();
    }

    @GetMapping("/activos")
    public List<EmpleadoOpcionResponse> listarActivos(HttpSession sesion) {
        sesionService.validarPersonal(sesion);
        Integer empleadoActualId = sesionService.obtenerEmpleadoId(sesion);
        return empleadoService.listarActivos().stream()
                .filter(empleado -> empleadoActualId == null || empleado.getId() != empleadoActualId)
                .map(EmpleadoOpcionResponse::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public EmpleadoResponse buscarPorId(@PathVariable int id, HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpleadoResponse guardar(@RequestBody EmpleadoRequest request, HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.guardar(
                request.nombre(),
                request.apellido(),
                request.usuario(),
                request.contrasenia()
        ));
    }

    @PutMapping("/{id}")
    public EmpleadoResponse actualizar(@PathVariable int id, @RequestBody EmpleadoRequest request,
                                       HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.actualizar(
                id,
                request.nombre(),
                request.apellido(),
                request.usuario(),
                request.contrasenia()
        ));
    }

    @PutMapping("/{id}/activar")
    public EmpleadoResponse activar(@PathVariable int id, HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.activar(id));
    }

    @PutMapping("/{id}/desactivar")
    public EmpleadoResponse desactivar(@PathVariable int id, HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.desactivar(id));
    }

    @PutMapping("/{id}/contrasenia")
    public EmpleadoResponse restablecerContrasenia(@PathVariable int id,
                                                    @RequestBody ContraseniaRequest request,
                                                    HttpSession sesion) {
        sesionService.validarDuenio(sesion);
        return EmpleadoResponse.desde(empleadoService.restablecerContrasenia(
                id,
                request.nuevaContrasenia(),
                request.contraseniaConfirmacion()
        ));
    }

    public record EmpleadoRequest(String nombre, String apellido, String usuario,
                                  String contrasenia) {
    }

    public record ContraseniaRequest(String nuevaContrasenia, String contraseniaConfirmacion) {
    }

    public record EmpleadoOpcionResponse(int id, String nombre, String usuario) {
        public static EmpleadoOpcionResponse desde(Empleado empleado) {
            return new EmpleadoOpcionResponse(
                    empleado.getId(),
                    empleado.getNombre() + " " + empleado.getApellido(),
                    empleado.getUsuario()
            );
        }
    }

    public record EmpleadoResponse(int id, String nombre, String apellido, String usuario,
                                   RolEmpleado rol, boolean activo) {
        public static EmpleadoResponse desde(Empleado empleado) {
            return new EmpleadoResponse(
                    empleado.getId(),
                    empleado.getNombre(),
                    empleado.getApellido(),
                    empleado.getUsuario(),
                    empleado.getRol(),
                    empleado.isActivo()
            );
        }
    }
}
