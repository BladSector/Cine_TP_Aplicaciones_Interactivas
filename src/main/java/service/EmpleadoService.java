package service;

import modelo.Empleado;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EmpleadoRepository;

import java.util.List;

@Service
public class EmpleadoService {
    private final EmpleadoRepository empleadoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(EmpleadoRepository empleadoRepository, PasswordEncoder passwordEncoder) {
        this.empleadoRepository = empleadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Empleado> listar() {
        return empleadoRepository.findAll();
    }

    public Empleado buscarPorId(int id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un empleado con ese id."));
    }

    public Empleado guardar(String nombre, String apellido, String usuario, String contrasenia) {
        validarDatos(nombre, apellido, usuario);
        validarContrasenia(contrasenia);
        String usuarioNormalizado = usuario.trim();
        if (empleadoRepository.existsByUsuarioIgnoreCase(usuarioNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un empleado con ese usuario.");
        }

        Empleado empleado = new Empleado(
                nombre.trim(),
                apellido.trim(),
                usuarioNormalizado,
                passwordEncoder.encode(contrasenia)
        );
        return empleadoRepository.save(empleado);
    }

    public Empleado actualizar(int id, String nombre, String apellido, String usuario,
                               String nuevaContrasenia) {
        validarDatos(nombre, apellido, usuario);
        String usuarioNormalizado = usuario.trim();
        if (empleadoRepository.existsByUsuarioIgnoreCaseAndIdNot(usuarioNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un empleado con ese usuario.");
        }

        Empleado empleado = buscarPorId(id);
        empleado.actualizarDatos(nombre.trim(), apellido.trim(), usuarioNormalizado);
        if (nuevaContrasenia != null && !nuevaContrasenia.isBlank()) {
            validarContrasenia(nuevaContrasenia);
            empleado.cambiarContrasenia(passwordEncoder.encode(nuevaContrasenia));
        }
        return empleadoRepository.save(empleado);
    }

    public Empleado autenticar(String usuario, String contrasenia) {
        if (usuario == null || usuario.isBlank() || contrasenia == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contrasenia incorrectos.");
        }

        Empleado empleado = empleadoRepository.findByUsuarioIgnoreCase(usuario.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contrasenia incorrectos."));
        if (!empleado.isActivo() || !passwordEncoder.matches(contrasenia, empleado.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contrasenia incorrectos.");
        }
        return empleado;
    }

    public Empleado activar(int id) {
        Empleado empleado = buscarPorId(id);
        empleado.activar();
        return empleadoRepository.save(empleado);
    }

    public Empleado desactivar(int id) {
        Empleado empleado = buscarPorId(id);
        empleado.desactivar();
        return empleadoRepository.save(empleado);
    }

    private void validarDatos(String nombre, String apellido, String usuario) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y el apellido son obligatorios.");
        }
        if (usuario == null || usuario.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario es obligatorio.");
        }
    }

    private void validarContrasenia(String contrasenia) {
        if (contrasenia == null || contrasenia.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia debe tener al menos 6 caracteres.");
        }
    }
}
