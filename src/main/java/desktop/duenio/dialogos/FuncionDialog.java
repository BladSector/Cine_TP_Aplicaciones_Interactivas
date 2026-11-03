package desktop.duenio.dialogos;

import java.awt.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import static desktop.comun.util.FormatoSwing.codigoFormato;
import static desktop.comun.util.FormatoSwing.codigoIdioma;
import static desktop.comun.util.FormatoSwing.nombreFormato;
import static desktop.comun.util.FormatoSwing.nombreIdioma;

public final class FuncionDialog {
    private FuncionDialog() {
    }

    public static Optional<Datos> mostrar(Component padre, Datos inicial,
                                          List<Opcion> peliculas, List<Opcion> salas) {
        boolean edicion = inicial != null;
        JTextField fecha = new JTextField(edicion ? inicial.fecha() : LocalDate.now().toString());
        JTextField horario = new JTextField(edicion ? inicial.horario() : "20:30");
        JTextField buscarPelicula = new JTextField();
        JTextField buscarSala = new JTextField();
        buscarPelicula.putClientProperty("JTextField.placeholderText", "Buscar por título...");
        buscarSala.putClientProperty("JTextField.placeholderText", "Buscar por nombre...");
        JComboBox<Opcion> pelicula = new JComboBox<>(peliculas.toArray(Opcion[]::new));
        JComboBox<Opcion> sala = new JComboBox<>(salas.toArray(Opcion[]::new));
        JComboBox<String> formato = new JComboBox<>(new String[]{"2D", "3D"});
        JComboBox<String> idioma = new JComboBox<>(new String[]{"ESPAÑOL", "SUBTITULADA"});
        JSpinner precio = new JSpinner(new SpinnerNumberModel(
                edicion ? inicial.precioEntrada() : 8000.0, 0.01, 10000000.0, 100.0));

        if (edicion) {
            FormularioDialogo.seleccionarPorId(pelicula, inicial.peliculaId());
            FormularioDialogo.seleccionarPorId(sala, inicial.salaId());
            formato.setSelectedItem(nombreFormato(inicial.formato()));
            idioma.setSelectedItem(nombreIdioma(inicial.idioma()));
        }
        FormularioDialogo.conectarBusqueda(buscarPelicula, pelicula, peliculas);
        FormularioDialogo.conectarBusqueda(buscarSala, sala, salas);

        JPanel panel = FormularioDialogo.crear(
                new String[]{"Fecha (AAAA-MM-DD)", "Horario (HH:mm)", "Buscar película", "Película",
                        "Buscar sala", "Sala", "Formato", "Idioma", "Precio"},
                fecha, horario, buscarPelicula, pelicula, buscarSala, sala, formato, idioma, precio);
        if (!FormularioDialogo.confirmar(padre, "Función", panel)) {
            return Optional.empty();
        }

        Opcion peliculaElegida = (Opcion) pelicula.getSelectedItem();
        Opcion salaElegida = (Opcion) sala.getSelectedItem();
        if (peliculaElegida == null || salaElegida == null) {
            FormularioDialogo.aviso(padre,
                    "Seleccioná una Película y una Sala de las opciones encontradas.");
            return Optional.empty();
        }

        return Optional.of(new Datos(
                fecha.getText().trim(),
                horario.getText().trim(),
                peliculaElegida.id(),
                salaElegida.id(),
                codigoFormato(String.valueOf(formato.getSelectedItem())),
                codigoIdioma(String.valueOf(idioma.getSelectedItem())),
                ((Number) precio.getValue()).doubleValue()));
    }

    public record Datos(String fecha, String horario, int peliculaId, int salaId,
                        String formato, String idioma, double precioEntrada) {
    }
}
