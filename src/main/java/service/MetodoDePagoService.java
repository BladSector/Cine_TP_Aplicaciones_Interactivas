package service;

import modelo.MetodoDePago;
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
        return metodoDePagoRepository.findAll();
    }

    public MetodoDePago buscarPorId(int id) {
        return metodoDePagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un metodo de pago con ese id."));
    }

    public MetodoDePago guardar(String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        try {
            return metodoDePagoRepository.save(new MetodoDePago(numero, fechaVencimiento, nombre, apellido, cvv));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public MetodoDePago actualizar(int id, String numero, YearMonth fechaVencimiento, String nombre, String apellido, String cvv) {
        try {
            MetodoDePago metodoDePago = buscarPorId(id);
            metodoDePago.actualizarDatos(numero, fechaVencimiento, nombre, apellido, cvv);
            return metodoDePagoRepository.save(metodoDePago);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void eliminar(int id) {
        MetodoDePago metodoDePago = buscarPorId(id);
        metodoDePagoRepository.delete(metodoDePago);
    }
}
