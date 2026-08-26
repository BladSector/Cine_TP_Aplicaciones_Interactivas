package service;

import modelo.Espectador;
import modelo.MetodoDePago;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.EspectadorRepository;
import repository.MetodoDePagoRepository;

import java.util.List;

@Service
public class EspectadorService {
    private final EspectadorRepository espectadorRepository;
    private final MetodoDePagoRepository metodoDePagoRepository;
    private final EmailService emailService;

    public EspectadorService(EspectadorRepository espectadorRepository,
                             MetodoDePagoRepository metodoDePagoRepository,
                             EmailService emailService) {
        this.espectadorRepository = espectadorRepository;
        this.metodoDePagoRepository = metodoDePagoRepository;
        this.emailService = emailService;
    }

    public List<Espectador> listar() {
        return espectadorRepository.findAll();
    }

    public Espectador buscarPorId(int id) {
        return espectadorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese id."));
    }

    public Espectador guardar(String nombre, String apellido, String email, String contrasenia) {
        Espectador espectador = espectadorRepository.save(new Espectador(nombre, apellido, email, contrasenia));
        emailService.enviarConfirmacionCuenta(espectador);
        return espectador;
    }

    public Espectador autenticar(String email, String contrasenia) {
        Espectador espectador = espectadorRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));

        if (contrasenia == null || !espectador.getContrasenia().equals(contrasenia)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia no es correcta.");
        }

        return espectador;
    }

    public Espectador actualizar(int id, String nombre, String apellido, String email,
                                 String contraseniaActual, String nuevaContrasenia,
                                 String nuevaContraseniaConfirmacion) {
        Espectador espectador = buscarPorId(id);
        String contraseniaActualizada = espectador.getContrasenia();

        if (nuevaContrasenia != null && !nuevaContrasenia.isBlank()) {
            validarCambioContrasenia(espectador, contraseniaActual, nuevaContrasenia, nuevaContraseniaConfirmacion);
            contraseniaActualizada = nuevaContrasenia;
        }

        espectador.actualizarDatos(nombre, apellido, email, contraseniaActualizada);
        return espectadorRepository.save(espectador);
    }

    public Espectador recuperarContrasenia(String email, String nuevaContrasenia, String nuevaContraseniaConfirmacion) {
        Espectador espectador = espectadorRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));

        if (nuevaContrasenia == null || nuevaContrasenia.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contrasenia no puede estar vacia.");
        }

        if (!nuevaContrasenia.equals(nuevaContraseniaConfirmacion)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contrasenias nuevas no coinciden.");
        }

        emailService.enviarRecuperacionContrasenia(espectador);
        espectador.actualizarDatos(espectador.getNombre(), espectador.getApellido(), espectador.getEmail(), nuevaContrasenia);
        return espectadorRepository.save(espectador);
    }

    public Espectador solicitarRecuperacionContrasenia(String email) {
        Espectador espectador = espectadorRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un espectador con ese email."));
        emailService.enviarRecuperacionContrasenia(espectador);
        return espectador;
    }

    public Espectador asociarMetodoDePago(int id, int metodoDePagoId) {
        Espectador espectador = buscarPorId(id);
        MetodoDePago metodoDePago = metodoDePagoRepository.findById(metodoDePagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));

        if (!metodoDePago.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago ya no esta activo.");
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

    public void eliminar(int id) {
        Espectador espectador = buscarPorId(id);
        espectadorRepository.delete(espectador);
    }

    private void validarCambioContrasenia(Espectador espectador, String contraseniaActual,
                                          String nuevaContrasenia, String nuevaContraseniaConfirmacion) {
        if (contraseniaActual == null || contraseniaActual.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá la contrasenia actual.");
        }

        if (!espectador.getContrasenia().equals(contraseniaActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasenia actual no es correcta.");
        }

        if (!nuevaContrasenia.equals(nuevaContraseniaConfirmacion)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contrasenias nuevas no coinciden.");
        }
    }
}
