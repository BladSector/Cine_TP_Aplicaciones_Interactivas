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
import java.util.List;
import java.util.Optional;

public final class PeliculaDialog {
    private PeliculaDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, Datos inicial, List<Opcion> categorias) {
        boolean edicion = inicial != null;
        JTextField titulo = new JTextField(edicion ? inicial.titulo() : "", 24);
        JSpinner duracion = new JSpinner(new SpinnerNumberModel(
                edicion ? inicial.duracion() : 100, 1, 600, 1));
        JTextArea descripcion = new JTextArea(edicion ? inicial.descripcion() : "", 3, 24);
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(descripcion);
        scrollDescripcion.setPreferredSize(new Dimension(320, 95));

        String[] portada = {edicion ? inicial.portadaUrl() : ""};
        JLabel vistaPrevia = new JLabel("Sin imagen", JLabel.CENTER);
        vistaPrevia.setPreferredSize(new Dimension(90, 120));
        vistaPrevia.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        SelectorImagenLocal.actualizarVistaPrevia(vistaPrevia, portada[0]);
        JButton elegirImagen = new JButton("Elegir imagen...");
        elegirImagen.addActionListener(evento ->
                portada[0] = SelectorImagenLocal.seleccionar(padre, portada[0], vistaPrevia));
        JPanel controlImagen = new JPanel(new BorderLayout(6, 6));
        controlImagen.add(vistaPrevia, BorderLayout.CENTER);
        controlImagen.add(elegirImagen, BorderLayout.SOUTH);

        JComboBox<Opcion> categoria = new JComboBox<>(categorias.toArray(Opcion[]::new));
        if (edicion) {
            FormularioDialogo.seleccionarPorNombre(categoria, inicial.categoriaNombre());
        }

        JPanel panel = FormularioDialogo.crear(
                new String[]{"Título", "Duración (min)", "Categoría", "Descripción", "Imagen de portada"},
                titulo, duracion, categoria, scrollDescripcion, controlImagen);
        if (!FormularioDialogo.confirmar(padre, "Película", panel)) {
            return Optional.empty();
        }

        Opcion categoriaElegida = (Opcion) categoria.getSelectedItem();
        if (categoriaElegida == null) {
            FormularioDialogo.aviso(padre, "Seleccioná una Categoría.");
            return Optional.empty();
        }
        return Optional.of(new Datos(
                titulo.getText().trim(),
                ((Number) duracion.getValue()).intValue(),
                descripcion.getText().trim(),
                portada[0],
                categoriaElegida.id(),
                categoriaElegida.nombre()));
    }

    public record Datos(String titulo, int duracion, String descripcion, String portadaUrl,
                        int categoriaId, String categoriaNombre) {
    }
}
