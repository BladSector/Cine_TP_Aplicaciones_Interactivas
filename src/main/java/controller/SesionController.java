package controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import service.AdminService;
import service.EmpleadoService;
import service.SesionService;
import modelo.Empleado;

import java.util.Map;

@RestController
@RequestMapping("/sesion")
public class SesionController {
    private final AdminService adminService;
    private final SesionService sesionService;
    private final EmpleadoService empleadoService;

    public SesionController(AdminService adminService, SesionService sesionService,
                            EmpleadoService empleadoService) {
        this.adminService = adminService;
        this.sesionService = sesionService;
        this.empleadoService = empleadoService;
    }

    @PostMapping("/admin")
    public Map<String, String> iniciarAdmin(@RequestBody AdminLoginRequest request, HttpSession sesion) {
        adminService.iniciarSesion(request.usuario(), request.contrasenia(), sesion);
        return Map.of("rol", "DUENIO");
    }

    @PostMapping("/empleado")
    public EmpleadoController.EmpleadoResponse iniciarEmpleado(@RequestBody EmpleadoLoginRequest request,
                                                                HttpSession sesion) {
        Empleado empleado = empleadoService.autenticar(request.usuario(), request.contrasenia());
        sesionService.iniciarEmpleado(sesion, empleado.getId(), empleado.getRol());
        return EmpleadoController.EmpleadoResponse.desde(empleado);
    }

    @PostMapping("/personal")
    public PersonalResponse iniciarPersonal(@RequestBody PersonalLoginRequest request, HttpSession sesion) {
        if (adminService.credencialesValidas(request.usuario(), request.contrasenia())) {
            sesionService.iniciarAdministrador(sesion);
            return new PersonalResponse(null, "Administrador", request.usuario(), "DUENIO");
        }

        Empleado empleado = empleadoService.autenticar(request.usuario(), request.contrasenia());
        sesionService.iniciarEmpleado(sesion, empleado.getId(), empleado.getRol());
        return new PersonalResponse(
                empleado.getId(),
                empleado.getNombre() + " " + empleado.getApellido(),
                empleado.getUsuario(),
                empleado.getRol().name()
        );
    }

    @GetMapping("/personal")
    public PersonalResponse obtenerPersonalActual(HttpSession sesion) {
        if (sesionService.esDuenio(sesion)) {
            return new PersonalResponse(null, "Administrador", "admin", "DUENIO");
        }
        Integer empleadoId = sesionService.obtenerEmpleadoId(sesion);
        if (empleadoId != null) {
            Empleado empleado = empleadoService.buscarPorId(empleadoId);
            return new PersonalResponse(
                    empleado.getId(),
                    empleado.getNombre() + " " + empleado.getApellido(),
                    empleado.getUsuario(),
                    empleado.getRol().name()
            );
        }
        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "No hay una sesion del personal activa."
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrar(HttpSession sesion) {
        sesionService.cerrar(sesion);
    }

    public record AdminLoginRequest(String usuario, String contrasenia) {
    }

    public record EmpleadoLoginRequest(String usuario, String contrasenia) {
    }

    public record PersonalLoginRequest(String usuario, String contrasenia) {
    }

    public record PersonalResponse(Integer id, String nombre, String usuario, String rol) {
    }
}
