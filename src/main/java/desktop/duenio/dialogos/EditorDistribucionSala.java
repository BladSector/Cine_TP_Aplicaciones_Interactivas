package desktop.duenio.dialogos;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EditorDistribucionSala extends JPanel {
    private static final Color BUTACA_ACTIVA = new Color(35, 142, 74);
    private static final Color BUTACA_INACTIVA = new Color(105, 111, 121);

    private final JTextField nombre = new JTextField(22);
    private final JSpinner filas;
    private final JSpinner columnas;
    private final JPanel mapa = new JPanel(new BorderLayout(0, 16));
    private final Set<String> activas = new HashSet<>();
    private int filasAnteriores;
    private int columnasAnteriores;

    private EditorDistribucionSala(String nombreSala, int cantidadFilas, int cantidadColumnas,
                                   Set<String> posicionesActivas) {
        super(new BorderLayout(0, 16));
        nombre.setText(nombreSala);
        filas = new JSpinner(new SpinnerNumberModel(cantidadFilas, 1, 26, 1));
        columnas = new JSpinner(new SpinnerNumberModel(cantidadColumnas, 1, 40, 1));
        filasAnteriores = cantidadFilas;
        columnasAnteriores = cantidadColumnas;
        activas.addAll(posicionesActivas);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controles.add(new JLabel("Nombre"));
        controles.add(nombre);
        controles.add(new JLabel("Filas"));
        controles.add(filas);
        controles.add(new JLabel("Butacas por fila"));
        controles.add(columnas);

        filas.addChangeListener(evento -> cambiarDimensiones());
        columnas.addChangeListener(evento -> cambiarDimensiones());

        JLabel ayuda = new JLabel(
                "Ajustá las filas y butacas. Hacé clic en cada lugar para personalizar la sala: verde es butaca y gris es espacio vacío.");
        ayuda.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 8));
        ayuda.setForeground(new Color(92, 98, 108));
        ayuda.setFont(ayuda.getFont().deriveFont(Font.ITALIC, 12f));

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(controles, BorderLayout.NORTH);
        encabezado.add(ayuda, BorderLayout.SOUTH);

        add(encabezado, BorderLayout.NORTH);
        add(mapa, BorderLayout.CENTER);
        dibujarMapa();
    }

    static EditorDistribucionSala nuevo() {
        int filas = 5;
        int columnas = 10;
        Set<String> posiciones = new HashSet<>();
        for (int fila = 0; fila < filas; fila++) {
            for (int numero = 1; numero <= columnas; numero++) {
                posiciones.add(posicion(fila, numero));
            }
        }
        return new EditorDistribucionSala("", filas, columnas, posiciones);
    }

    static EditorDistribucionSala desde(String nombre, List<JsonNode> butacas) {
        int cantidadFilas = 1;
        int cantidadColumnas = 1;
        Set<String> posiciones = new HashSet<>();

        for (JsonNode butaca : butacas) {
            String fila = butaca.path("fila").asText("A").trim().toUpperCase();
            int indiceFila = Math.max(0, fila.charAt(0) - 'A');
            int numero = Math.max(1, butaca.path("numero").asInt(1));
            cantidadFilas = Math.max(cantidadFilas, indiceFila + 1);
            cantidadColumnas = Math.max(cantidadColumnas, numero);
            posiciones.add(posicion(indiceFila, numero));
        }
        return new EditorDistribucionSala(nombre, cantidadFilas, cantidadColumnas, posiciones);
    }

    String getNombre() {
        return nombre.getText().trim();
    }

    List<Map<String, Object>> getUbicaciones() {
        int cantidadFilas = (Integer) filas.getValue();
        int cantidadColumnas = (Integer) columnas.getValue();
        return activas.stream()
                .map(EditorDistribucionSala::leerPosicion)
                .filter(posicion -> posicion.fila() < cantidadFilas && posicion.numero() <= cantidadColumnas)
                .sorted(Comparator.comparingInt(PosicionSala::fila).thenComparingInt(PosicionSala::numero))
                .map(posicion -> Map.<String, Object>of(
                        "fila", String.valueOf((char) ('A' + posicion.fila())),
                        "numero", posicion.numero()))
                .toList();
    }

    private void cambiarDimensiones() {
        int nuevasFilas = (Integer) filas.getValue();
        int nuevasColumnas = (Integer) columnas.getValue();
        activas.removeIf(clave -> {
            PosicionSala posicion = leerPosicion(clave);
            return posicion.fila() >= nuevasFilas || posicion.numero() > nuevasColumnas;
        });

        for (int fila = 0; fila < nuevasFilas; fila++) {
            for (int numero = 1; numero <= nuevasColumnas; numero++) {
                if (fila >= filasAnteriores || numero > columnasAnteriores) {
                    activas.add(posicion(fila, numero));
                }
            }
        }

        filasAnteriores = nuevasFilas;
        columnasAnteriores = nuevasColumnas;
        dibujarMapa();
    }

    private void dibujarMapa() {
        mapa.removeAll();
        int cantidadFilas = (Integer) filas.getValue();
        int cantidadColumnas = (Integer) columnas.getValue();

        JLabel pantalla = new JLabel("PANTALLA", JLabel.CENTER);
        pantalla.setOpaque(true);
        pantalla.setBackground(new Color(39, 43, 50));
        pantalla.setForeground(Color.WHITE);
        pantalla.setFont(pantalla.getFont().deriveFont(Font.BOLD, 15f));
        pantalla.setPreferredSize(new Dimension(Math.max(520, cantidadColumnas * 55), 42));
        pantalla.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JPanel grilla = new JPanel(new GridBagLayout());
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(3, 3, 3, 3);
        restricciones.fill = GridBagConstraints.BOTH;

        for (int fila = 0; fila < cantidadFilas; fila++) {
            restricciones.gridx = 0;
            restricciones.gridy = fila;
            restricciones.weightx = 0;
            JLabel etiquetaFila = new JLabel(String.valueOf((char) ('A' + fila)), JLabel.CENTER);
            etiquetaFila.setPreferredSize(new Dimension(28, 34));
            grilla.add(etiquetaFila, restricciones);

            for (int numero = 1; numero <= cantidadColumnas; numero++) {
                String clave = posicion(fila, numero);
                JButton butaca = new JButton(String.valueOf(numero));
                butaca.setPreferredSize(new Dimension(48, 34));
                butaca.setMargin(new Insets(2, 4, 2, 4));
                butaca.setFocusPainted(false);
                pintarButaca(butaca, activas.contains(clave));
                butaca.addActionListener(evento -> {
                    if (!activas.remove(clave)) {
                        activas.add(clave);
                    }
                    pintarButaca(butaca, activas.contains(clave));
                });

                restricciones.gridx = numero;
                restricciones.weightx = 1;
                grilla.add(butaca, restricciones);
            }
        }

        JPanel contenido = new JPanel(new BorderLayout(0, 16));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 16, 12));
        contenido.add(pantalla, BorderLayout.NORTH);
        contenido.add(grilla, BorderLayout.CENTER);
        mapa.add(contenido, BorderLayout.NORTH);
        mapa.revalidate();
        mapa.repaint();
    }

    private static void pintarButaca(JButton butaca, boolean activa) {
        butaca.setOpaque(true);
        butaca.setBackground(activa ? BUTACA_ACTIVA : BUTACA_INACTIVA);
        butaca.setForeground(Color.WHITE);
        butaca.setToolTipText(activa ? "Butaca incluida" : "Espacio sin butaca");
    }

    private static String posicion(int fila, int numero) {
        return fila + "-" + numero;
    }

    private static PosicionSala leerPosicion(String clave) {
        String[] partes = clave.split("-", 2);
        return new PosicionSala(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
    }

    private record PosicionSala(int fila, int numero) {
    }
}
