package service;

import modelo.Espectador;
import modelo.MetodoDePago;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EspectadorRepository;
import repository.MetodoDePagoRepository;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class EspectadorService {
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final EspectadorRepository espectadorRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public EspectadorService(EspectadorRepository espectadorRepository,
                             MetodoDePagoRepository metodoDePagoRepository,
                             EmailService emailService,
                             PasswordEncoder passwordEncoder) {
        this.espectadorRepository = espectadorRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Espectador> listar() {
        return espectadorRepository.findAll();
    }

    public Espectador buscarPorId(int id) {
        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
    }

    public Espectador guardar(String nombre, String apellido, String email, String contrasenia) {
        return guardar(nombre, apellido, email, contrasenia, contrasenia);
    }

    public Espectador guardar(String nombre, String apellido, String email, String contrasenia,
                              String contraseniaConfirmacion) {
        validarDatosPersonales(nombre, apellido, email);
        validarNuevaContrasenia(contrasenia, contraseniaConfirmacion);
        String emailNormalizado = normalizarEmail(email);
        if (espectadorRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una cuenta con ese email.");
        }

        Espectador espectador = new Espectador(
                nombre.trim(), apellido.trim(), emailNormalizado, passwordEncoder.encode(contrasenia)
        );
        espectador.asignarTokenVerificacionEmail(UUID.randomUUID().toString());
        espectador = espectadorRepository.save(espectador);
        emailService.enviarConfirmacionCuenta(espectador, espectador.getTokenVerificacionEmail());
        return espectador;
    }

    public Espectador autenticar(String email, String contrasenia) {
        if (email == null || contrasenia == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contrasenia son obligatorios.");
        }
        Espectador espectador = espectadorRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));

        if (!contraseniaCorrecta(contrasenia, espectador.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia no es correcta.");
        }

        if (!esBCrypt(espectador.getContrasenia())) {
            espectador.actualizarDatos(espectador.getNombre(), espectador.getApellido(), espectador.getEmail(),
                    passwordEncoder.encode(contrasenia));
            espectadorRepository.save(espectador);
        }

        return espectador;
    }

    public Espectador actualizar(int id, String nombre, String apellido, String email,
                                 String contraseniaActual, String nuevaContrasenia,
                                 String nuevaContraseniaConfirmacion) {
        Espectador espectador = buscarPorId(id);
        validarDatosPersonales(nombre, apellido, email);
        String emailNormalizado = normalizarEmail(email);
        espectadorRepository.findByEmailIgnoreCase(emailNormalizado)
                .filter(otro -> otro.getId() != id)
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una cuenta con ese email.");
                });
        String contraseniaActualizada = espectador.getContrasenia();

        if (nuevaContrasenia != null && !nuevaContrasenia.isBlank()) {
            validarCambioContrasenia(espectador, contraseniaActual, nuevaContrasenia, nuevaContraseniaConfirmacion);
            contraseniaActualizada = passwordEncoder.encode(nuevaContrasenia);
        }

        espectador.actualizarDatos(nombre.trim(), apellido.trim(), emailNormalizado, contraseniaActualizada);
        return espectadorRepository.save(espectador);
    }

    public Espectador recuperarContrasenia(String email, String token, String nuevaContrasenia,
                                           String nuevaContraseniaConfirmacion) {
        validarNuevaContrasenia(nuevaContrasenia, nuevaContraseniaConfirmacion);
        Espectador espectador = espectadorRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));
        if (token == null || !token.equals(espectador.getTokenRecuperacionContrasenia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace de recuperacion no es valido.");
        }

        espectador.actualizarDatos(espectador.getNombre(), espectador.getApellido(), espectador.getEmail(),
                passwordEncoder.encode(nuevaContrasenia));
        espectador.asignarTokenRecuperacionContrasenia(null);
        return espectadorRepository.save(espectador);
    }

    public SolicitudRecuperacion solicitarRecuperacionContrasenia(String email) {
        Espectador espectador = espectadorRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));
        String token = UUID.randomUUID().toString();
        espectador.asignarTokenRecuperacionContrasenia(token);
        espectadorRepository.save(espectador);
        emailService.enviarRecuperacionContrasenia(espectador, token);
        return new SolicitudRecuperacion(espectador, token);
    }

    public Espectador asociarMetodoDePago(int id, int metodoDePagoId) {
        Espectador espectador = buscarPorId(id);
        MetodoDePago metodoDePago = metodoDePagoRepository.findById(metodoDePagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));

        if (!metodoDePago.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago ya no esta activo.");
        }
        if (metodoDePago.getEspectador() != null && metodoDePago.getEspectador().getId() != espectador.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago pertenece a otro espectador.");
        }

        espectador.agregarMetodoDePago(metodoDePago);
        metodoDePagoRepository.save(metodoDePago);
        return espectadorRepository.save(espectador);
    }

    public Espectador verificarMail(int id) {
        Espectador espectador = buscarPorId(id);
        espectador.verificarMail();
        return espectadorRepository.save(espectador);
    }

    public Espectador verificarMail(String token) {
        Espectador espectador = espectadorRepository.findByTokenVerificacionEmail(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace de verificacion no es valido."));
        espectador.verificarMail();
        espectador.asignarTokenVerificacionEmail(null);
        return espectadorRepository.save(espectador);
    }

    public void eliminar(int id) {
        Espectador espectador = buscarPorId(id);
        espectadorRepository.delete(espectador);
    }

    private void validarCambioContrasenia(Espectador espectador, String contraseniaActual,
                                          String nuevaContrasenia, String nuevaContraseniaConfirmacion) {
        if (contraseniaActual == null || contraseniaActual.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá la contrasenia actual.");
        }

        if (!contraseniaCorrecta(contraseniaActual, espectador.getContrasenia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia actual no es correcta.");
        }
        validarNuevaContrasenia(nuevaContrasenia, nuevaContraseniaConfirmacion);
    }

    private void validarDatosPersonales(String nombre, String apellido, String email) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre y apellido son obligatorios.");
        }
        if (email == null || !EMAIL_VALIDO.matcher(email.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no es valido.");
        }
    }

    private void validarNuevaContrasenia(String contrasenia, String confirmacion) {
        if (contrasenia == null || contrasenia.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia debe tener al menos 6 caracteres.");
        }
        if (!contrasenia.equals(confirmacion)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contrasenias no coinciden.");
        }
    }

    private boolean contraseniaCorrecta(String ingresada, String guardada) {
        return esBCrypt(guardada) ? passwordEncoder.matches(ingresada, guardada) : guardada.equals(ingresada);
    }

    private boolean esBCrypt(String contrasenia) {
        return contrasenia != null && contrasenia.matches("^\\$2[ayb]\\$.{56}$");
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public record SolicitudRecuperacion(Espectador espectador, String token) {
    }
}
