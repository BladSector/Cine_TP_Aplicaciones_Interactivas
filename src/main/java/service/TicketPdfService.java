package service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import modelo.Entrada;
import modelo.ItemConsumo;
import modelo.Ticket;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
public class TicketPdfService {
    private static final float MARGEN = 48;
    private static final float INTERLINEADO = 18;

    public byte[] generar(Ticket ticket, double total) {
        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float y = pagina.getMediaBox().getHeight() - MARGEN;

                escribir(contenido, negrita, 22, MARGEN, y, "CINE API - TICKET");
                y -= 34;
                escribir(contenido, normal, 11, MARGEN, y, "Codigo: " + ticket.getCodigoQR());
                y -= INTERLINEADO;
                escribir(contenido, normal, 11, MARGEN, y,
                        "Espectador: " + ticket.getEspectador().getNombre() + " " + ticket.getEspectador().getApellido());
                y -= INTERLINEADO;
                escribir(contenido, normal, 11, MARGEN, y,
                        "Pago: " + textoSeguro(ticket.getMetodoDePagoResumen()));

                byte[] qr = generarQr(ticket.getCodigoQR());
                PDImageXObject imagenQr = PDImageXObject.createFromByteArray(documento, qr, "ticket-qr");
                contenido.drawImage(imagenQr, 410, pagina.getMediaBox().getHeight() - 175, 120, 120);

                y -= 34;
                escribir(contenido, negrita, 15, MARGEN, y, "Entradas");
                y -= 22;
                for (Entrada entrada : ticket.getEntradas()) {
                    escribir(contenido, negrita, 11, MARGEN, y,
                            recortar(entrada.getFuncion().getPelicula().getTitulo(), 70));
                    y -= INTERLINEADO;
                    escribir(contenido, normal, 10, MARGEN + 12, y,
                            entrada.getFuncion().getFecha() + " " + entrada.getFuncion().getHorario()
                                    + " | " + entrada.getFuncion().getFormato()
                                    + " | " + entrada.getFuncion().getIdioma());
                    y -= INTERLINEADO;
                    escribir(contenido, normal, 10, MARGEN + 12, y,
                            "Sala " + entrada.getFuncion().getSala().getNombre()
                                    + " | Butaca " + entrada.getButaca().getFila() + entrada.getButaca().getNumero()
                                    + " | $" + formatoPrecio(entrada.getPrecio()));
                    y -= 24;
                }

                if (!ticket.getItemsConsumo().isEmpty()) {
                    escribir(contenido, negrita, 15, MARGEN, y, "Consumos");
                    y -= 22;
                    for (ItemConsumo item : ticket.getItemsConsumo()) {
                        double subtotal = item.getProducto().getPrecio() * item.getCantidad();
                        escribir(contenido, normal, 10, MARGEN + 12, y,
                                recortar(item.getProducto().getNombre(), 48)
                                        + " x" + item.getCantidad()
                                        + " | $" + formatoPrecio(subtotal));
                        y -= INTERLINEADO;
                    }
                    y -= 8;
                }

                escribir(contenido, negrita, 16, MARGEN, y, "TOTAL: $" + formatoPrecio(total));
                y -= 30;
                escribir(contenido, normal, 9, MARGEN, y,
                        "Presenta este codigo QR al ingresar y para retirar tus consumos.");
            }

            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException | WriterException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el PDF del ticket.", e);
        }
    }

    private byte[] generarQr(String codigo) throws WriterException, IOException {
        BitMatrix matriz = new QRCodeWriter().encode(codigo, BarcodeFormat.QR_CODE, 280, 280);
        BufferedImage imagen = new BufferedImage(matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matriz.getWidth(); x++) {
            for (int y = 0; y < matriz.getHeight(); y++) {
                imagen.setRGB(x, y, matriz.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "PNG", salida);
            return salida.toByteArray();
        }
    }

    private void escribir(PDPageContentStream contenido, PDType1Font fuente, float tamano,
                          float x, float y, String texto) throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(x, y);
        contenido.showText(textoSeguro(texto));
        contenido.endText();
    }

    private String textoSeguro(String texto) {
        return texto == null ? "-" : texto.replace('\n', ' ').replace('\r', ' ');
    }

    private String recortar(String texto, int maximo) {
        String valor = textoSeguro(texto);
        return valor.length() <= maximo ? valor : valor.substring(0, maximo - 3) + "...";
    }

    private String formatoPrecio(double precio) {
        return String.format(Locale.US, "%.2f", precio);
    }
}
