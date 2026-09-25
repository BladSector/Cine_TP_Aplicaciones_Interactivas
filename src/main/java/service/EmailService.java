package service;

import modelo.Entrada;
import modelo.Espectador;
import modelo.ItemConsumo;
import modelo.Ticket;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final boolean mailHabilitado;
    private final String remitente;
    private final String baseUrl;
    private final TicketPdfService ticketPdfService;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        @Value("${app.mail.enabled:false}") boolean mailHabilitado,
                        @Value("${spring.mail.username:}") String remitente,
                        @Value("${app.base-url:http://localhost:8080}") String baseUrl,
                        TicketPdfService ticketPdfService) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.mailHabilitado = mailHabilitado;
        this.remitente = remitente;
        this.baseUrl = baseUrl;
        this.ticketPdfService = ticketPdfService;
    }

    public void enviarConfirmacionCuenta(Espectador espectador, String token) {
        String enlace = baseUrl + "/espectadores/verificar-mail?token=" + token;
        enviar(espectador.getEmail(), "Confirma tu cuenta de Cine API",
                "Hola " + espectador.getNombre() + ", confirma tu cuenta en: " + enlace);
    }

    public void enviarRecuperacionContrasenia(Espectador espectador, String token) {
        String enlace = baseUrl + "/recuperar-contrasenia?token=" + token;
        enviar(espectador.getEmail(), "Recupera tu contrasenia de Cine API",
                "Hola " + espectador.getNombre() + ", cambia tu contrasenia en: " + enlace);
    }

    public void enviarTicket(Ticket ticket, double total) {
        StringJoiner cuerpo = new StringJoiner(System.lineSeparator());
        cuerpo.add("Codigo QR: " + ticket.getCodigoQR());
        cuerpo.add("Espectador: " + ticket.getEspectador().getNombre() + " " + ticket.getEspectador().getApellido());

        for (Entrada entrada : ticket.getEntradas()) {
            cuerpo.add("Entrada: "
                    + entrada.getFuncion().getPelicula().getTitulo()
                    + " | " + entrada.getFuncion().getFecha()
                    + " " + entrada.getFuncion().getHorario()
                    + " | Sala " + entrada.getFuncion().getSala().getNombre()
                    + " | Butaca " + entrada.getButaca().getFila() + entrada.getButaca().getNumero());
        }

        for (ItemConsumo itemConsumo : ticket.getItemsConsumo()) {
            cuerpo.add("Consumo: "
                    + itemConsumo.getProducto().getNombre()
                    + " x" + itemConsumo.getCantidad());
        }

        cuerpo.add("Total: $" + total);
        byte[] pdf = ticketPdfService.generar(ticket, total);
        enviarConAdjunto(
                ticket.getEspectador().getEmail(),
                "Tu ticket de Cine API",
                cuerpo.toString(),
                "ticket-" + ticket.getId() + ".pdf",
                pdf
        );
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        if (!mailHabilitado || mailSender == null) {
            System.out.println("[MAIL SIMULADO] Para: " + destinatario);
            System.out.println("[MAIL SIMULADO] Asunto: " + asunto);
            System.out.println(cuerpo);
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        if (remitente != null && !remitente.isBlank()) {
            mensaje.setFrom(remitente);
        }
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }

    private void enviarConAdjunto(String destinatario, String asunto, String cuerpo,
                                  String nombreArchivo, byte[] archivo) {
        if (!mailHabilitado || mailSender == null) {
            System.out.println("[MAIL SIMULADO] Para: " + destinatario);
            System.out.println("[MAIL SIMULADO] Asunto: " + asunto);
            System.out.println(cuerpo);
            System.out.println("[MAIL SIMULADO] Adjunto: " + nombreArchivo + " (" + archivo.length + " bytes)");
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            if (remitente != null && !remitente.isBlank()) {
                helper.setFrom(remitente);
            }
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo);
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivo), "application/pdf");
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo adjuntar el PDF al correo.", e);
        }
    }
}
