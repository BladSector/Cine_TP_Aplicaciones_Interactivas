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

    public Espectador actualizar(int id, String nombre, String apellido, String email, String contrasenia) {
        Espectador espectador = buscarPorId(id);
        String contraseniaActualizada = contrasenia == null || contrasenia.isBlank()
                ? espectador.getContrasenia()
                : contrasenia;
        espectador.actualizarDatos(nombre, apellido, email, contraseniaActualizada);
        return espectadorRepository.save(espectador);
    }

    public Espectador asociarMetodoDePago(int id, int metodoDePagoId) {
        Espectador espectador = buscarPorId(id);
        MetodoDePago metodoDePago = metodoDePagoRepository.findById(metodoDePagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));
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
}
