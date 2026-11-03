package desktop.duenio.dialogos;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

final class SelectorImagenLocal {
    private static final int TAMANO_MAXIMO = 5 * 1024 * 1024;

    private SelectorImagenLocal() {
    }

    static String seleccionar(Component padre, String imagenActual, JLabel vistaPrevia) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Elegir imagen");
        selector.setFileFilter(new FileNameExtensionFilter(
                "Imágenes JPG, PNG o WEBP", "jpg", "jpeg", "png", "webp"));

        if (selector.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return imagenActual;
        }

        File archivo = selector.getSelectedFile();
        try {
            byte[] contenido = Files.readAllBytes(archivo.toPath());
            if (contenido.length > TAMANO_MAXIMO) {
                FormularioDialogo.aviso(padre, "La imagen no puede superar los 5 MB.");
                return imagenActual;
            }

            String mime = Files.probeContentType(archivo.toPath());
            if (mime == null || !mime.startsWith("image/")) {
                mime = mimePorExtension(archivo.getName());
            }
            if (mime == null) {
                FormularioDialogo.aviso(padre, "El archivo elegido no es una imagen JPG, PNG o WEBP.");
                return imagenActual;
            }

            String imagen = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(contenido);
            actualizarVistaPrevia(vistaPrevia, imagen);
            return imagen;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(padre,
                    "No se pudo leer la imagen: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return imagenActual;
        }
    }

    static void actualizarVistaPrevia(JLabel vistaPrevia, String imagenCodificada) {
        vistaPrevia.setIcon(null);
        if (imagenCodificada == null || imagenCodificada.isBlank()) {
            vistaPrevia.setText("Sin imagen");
            return;
        }
        if (!imagenCodificada.startsWith("data:image/") || !imagenCodificada.contains(",")) {
            vistaPrevia.setText("Imagen actual");
            return;
        }

        try {
            String base64 = imagenCodificada.substring(imagenCodificada.indexOf(',') + 1);
            ImageIcon original = new ImageIcon(Base64.getDecoder().decode(base64));
            if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
                throw new IllegalArgumentException("Imagen inválida");
            }

            int anchoMaximo = Math.max(1, vistaPrevia.getPreferredSize().width - 8);
            int altoMaximo = Math.max(1, vistaPrevia.getPreferredSize().height - 8);
            double escala = Math.min(
                    (double) anchoMaximo / original.getIconWidth(),
                    (double) altoMaximo / original.getIconHeight());
            int ancho = Math.max(1, (int) (original.getIconWidth() * escala));
            int alto = Math.max(1, (int) (original.getIconHeight() * escala));
            Image imagen = original.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            vistaPrevia.setText("");
            vistaPrevia.setIcon(new ImageIcon(imagen));
        } catch (IllegalArgumentException e) {
            vistaPrevia.setText("Imagen inválida");
        }
    }

    private static String mimePorExtension(String nombre) {
        String minuscula = nombre.toLowerCase();
        if (minuscula.endsWith(".jpg") || minuscula.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (minuscula.endsWith(".png")) {
            return "image/png";
        }
        if (minuscula.endsWith(".webp")) {
            return "image/webp";
        }
        return null;
    }
}
