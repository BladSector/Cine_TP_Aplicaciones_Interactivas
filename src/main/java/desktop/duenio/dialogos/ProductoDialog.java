package desktop.duenio.dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Optional;

public final class ProductoDialog {
    private ProductoDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, Datos inicial) {
        boolean edicion = inicial != null;
        JTextField nombre = new JTextField(edicion ? inicial.nombre() : "");
        JTextArea descripcion = new JTextArea(edicion ? inicial.descripcion() : "", 3, 22);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        JScrollPane descripcionScroll = new JScrollPane(descripcion);
        descripcionScroll.setPreferredSize(new Dimension(320, 72));
        JSpinner precio = new JSpinner(new SpinnerNumberModel(
                edicion ? inicial.precio() : 1000.0, 0.01, 10000000.0, 100.0));
        JComboBox<String> tipo = new JComboBox<>(new String[]{"POCHOCLOS", "BEBIDA", "DULCE", "COMBO"});
        JComboBox<String> tamano = new JComboBox<>(new String[]{"CHICO", "MEDIANO", "GRANDE", "UNICO"});
        if (edicion) {
            tipo.setSelectedItem(inicial.tipo());
            tamano.setSelectedItem(inicial.tamano());
        }

        String[] imagen = {edicion ? inicial.imagenUrl() : ""};
        JLabel vistaPrevia = new JLabel("Sin imagen", JLabel.CENTER);
        vistaPrevia.setPreferredSize(new Dimension(140, 105));
        vistaPrevia.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        SelectorImagenLocal.actualizarVistaPrevia(vistaPrevia, imagen[0]);
        JButton elegirImagen = new JButton("Elegir imagen...");
        elegirImagen.addActionListener(evento ->
                imagen[0] = SelectorImagenLocal.seleccionar(padre, imagen[0], vistaPrevia));
        JPanel controlImagen = new JPanel(new BorderLayout(6, 6));
        controlImagen.add(vistaPrevia, BorderLayout.CENTER);
        controlImagen.add(elegirImagen, BorderLayout.SOUTH);

        JPanel panel = FormularioDialogo.crear(
                new String[]{"Nombre", "Descripción", "Precio", "Tipo", "Tamaño", "Imagen"},
                nombre, descripcionScroll, precio, tipo, tamano, controlImagen);
        if (!FormularioDialogo.confirmar(padre, "Producto de confitería", panel)) {
            return Optional.empty();
        }

        return Optional.of(new Datos(
                nombre.getText().trim(),
                descripcion.getText().trim(),
                ((Number) precio.getValue()).doubleValue(),
                String.valueOf(tipo.getSelectedItem()),
                String.valueOf(tamano.getSelectedItem()),
                imagen[0]));
    }

    public record Datos(String nombre, String descripcion, double precio, String tipo,
                        String tamano, String imagenUrl) {
    }
}
