package service;

import modelo.Entrada;
import modelo.Espectador;
import modelo.ItemConsumo;
import modelo.Ticket;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void enviarConfirmacionCuenta(Espectador espectador) {
        String mensaje = "Confirmacion enviada a " + espectador.getEmail()
                + ". Link ilustrativo: http://localhost:8080/espectadores/"
                + espectador.getId()
                + "/verificar-mail";

        System.out.println(mensaje);
    }

    public void enviarRecuperacionContrasenia(Espectador espectador) {
        String mensaje = "Recuperacion de contrasenia enviada a " + espectador.getEmail()
                + ". Link ilustrativo: http://localhost:8080/recuperar-contrasenia?espectadorId="
                + espectador.getId();

        System.out.println(mensaje);
    }

    public void enviarTicket(Ticket ticket) {
        System.out.println("Ticket enviado a " + ticket.getEspectador().getEmail());
        System.out.println("Codigo QR: " + ticket.getCodigoQR());
        System.out.println("Espectador: " + ticket.getEspectador().getNombre() + " " + ticket.getEspectador().getApellido());

        for (Entrada entrada : ticket.getEntradas()) {
            System.out.println("Entrada: "
                    + entrada.getFuncion().getPelicula().getTitulo()
                    + " | " + entrada.getFuncion().getFecha()
                    + " " + entrada.getFuncion().getHorario()
                    + " | Sala " + entrada.getFuncion().getSala().getNombre()
                    + " | Butaca " + entrada.getButaca().getFila() + entrada.getButaca().getNumero());
        }

        for (ItemConsumo itemConsumo : ticket.getItemsConsumo()) {
            System.out.println("Consumo: "
                    + itemConsumo.getProducto().getNombre()
                    + " x" + itemConsumo.getCantidad());
        }

        System.out.println("Total: $" + ticket.calcularTotal());
    }
}
