package service;

import modelo.MetodoDePago;
import modelo.Espectador;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import repository.MetodoDePagoRepository;

import java.time.YearMonth;
import java.util.List;

@Service
public class MetodoDePagoService {
    private final MetodoDePagoRepository metodoDePagoRepository;

    public MetodoDePagoService(MetodoDePagoRepository metodoDePagoRepository) {
        this.metodoDePagoRepository = metodoDePagoRepository;
    }

    public List<MetodoDePago> listar() {
        return metodoDePagoRepository.findAll().stream()
                .filter(MetodoDePago::isActiva)
                .toList();
    }

    public MetodoDePago buscarPorId(int id) {
        return metodoDePagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));
    }

    public MetodoDePago guardar(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        validarDatos(numero, fechaVencimiento, nombre, cvv);
        return metodoDePagoRepository.save(new MetodoDePago(numero, fechaVencimiento, nombre.trim(), normalizar(apellido), cvv));
    }

    public MetodoDePago actualizar(int id, String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        validarDatos(numero, fechaVencimiento, nombre, cvv);
        MetodoDePago metodoDePago = buscarPorId(id);
        if (!metodoDePago.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede modificar un metodo de pago inactivo.");
        }
        metodoDePago.actualizarDatos(numero, fechaVencimiento, nombre.trim(), normalizar(apellido), cvv);
        return metodoDePagoRepository.save(metodoDePago);
    }

    public void eliminar(int id) {
        MetodoDePago metodoDePago = buscarPorId(id);
        metodoDePago.desactivar();
        metodoDePagoRepository.save(metodoDePago);
    }

    public MetodoDePago validarParaCompra(int metodoDePagoId, Espectador espectador) {
        MetodoDePago metodoDePago = buscarPorId(metodoDePagoId);

        if (metodoDePago.getEspectador() == null || metodoDePago.getEspectador().getId() != espectador.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago no pertenece al espectador.");
        }
        if (!metodoDePago.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago ya no esta activo.");
        }
        if (estaVencido(metodoDePago)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El metodo de pago esta vencido.");
        }
        return metodoDePago;
    }

    public boolean estaVencido(MetodoDePago metodoDePago) {
        return metodoDePago.getFechaVencimiento() == null
                || !metodoDePago.getFechaVencimiento().isAfter(YearMonth.now());
    }

    public String crearResumen(MetodoDePago metodoDePago) {
        return "Tarjeta terminada en " + metodoDePago.getUltimosNumeros();
    }

    private void validarDatos(String numero, YearMonth fechaVencimiento, String nombre, String cvv) {
        if (numero == null || !numero.matches("\\d{16}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El numero de tarjeta debe tener exactamente 16 digitos.");
        }
        if (fechaVencimiento == null || !fechaVencimiento.isAfter(YearMonth.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento debe ser posterior al mes actual.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El titular de la tarjeta es obligatorio.");
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El CVV debe tener exactamente 3 digitos.");
        }
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
