package desktop.comun.componentes;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;
import static desktop.comun.util.FormatoSwing.nombreEstadoButaca;

public final class MapaButacasPanel extends JPanel {
    private final Consumer<Integer> alSeleccionar;
    private final Consumer<JsonNode> alVerDetalle;

    public MapaButacasPanel(Consumer<Integer> alSeleccionar, Consumer<JsonNode> alVerDetalle) {
        super(new BorderLayout());
        this.alSeleccionar = alSeleccionar;
        this.alVerDetalle = alVerDetalle;
    }

    public void mostrarSinSala() {
        removeAll();
        add(new JLabel("No hay salas cargadas.", JLabel.CENTER), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void mostrar(List<JsonNode> butacas, Integer seleccionadaId) {
        removeAll();
        JPanel contenido = new JPanel(new BorderLayout(0, 20));
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        contenido.add(new PantallaPanel(), BorderLayout.NORTH);
        JPanel filasPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        List<String> filas = butacas.stream().map(butaca -> butaca.path("fila").asText()).distinct().toList();
        int maximoNumero = butacas.stream().mapToInt(butaca -> butaca.path("numero").asInt()).max().orElse(0);
        for (String fila : filas) {
            JPanel filaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            JLabel etiqueta = new JLabel(fila);
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
            etiqueta.setPreferredSize(new Dimension(24, 38));
            filaPanel.add(etiqueta);
            for (int numero = 1; numero <= maximoNumero; numero++) {
                JsonNode butaca = buscarButaca(butacas, fila, numero);
                if (butaca == null) filaPanel.add(Box.createRigidArea(new Dimension(58, 38)));
                else filaPanel.add(crearBoton(butaca, seleccionadaId));
            }
            filasPanel.add(filaPanel);
        }
        if (butacas.isEmpty()) filasPanel.add(new JLabel("Esta sala no tiene butacas registradas.", JLabel.CENTER));
        contenido.add(filasPanel, BorderLayout.CENTER);
        contenido.add(crearLeyenda(), BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JButton crearBoton(JsonNode butaca, Integer seleccionadaId) {
        int id = butaca.path("id").asInt();
        String estado = butaca.path("estado").asText("DISPONIBLE");
        String nombre = butaca.path("fila").asText() + butaca.path("numero").asInt();
        JButton boton = new JButton(nombre);
        boton.setUI(new BasicButtonUI());
        boton.setPreferredSize(new Dimension(58, 38));
        boton.setMinimumSize(new Dimension(58, 38));
        boton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setFocusPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
        boton.setBackground(colorEstado(estado));
        boton.setForeground(Color.WHITE);
        boton.setToolTipText("Butaca " + nombre + " - " + nombreEstadoButaca(estado));
        if (seleccionadaId != null && seleccionadaId == id) {
            boton.setBorder(BorderFactory.createLineBorder(new Color(30, 90, 180), 3));
        }
        boton.addActionListener(event -> alSeleccionar.accept(id));
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    alVerDetalle.accept(butaca);
                }
            }
        });
        return boton;
    }

    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        leyenda.add(elementoLeyenda("Disponible", colorEstado("DISPONIBLE")));
        leyenda.add(elementoLeyenda("Ocupada", colorEstado("OCUPADA")));
        leyenda.add(elementoLeyenda("Fuera de servicio", colorEstado("FUERA_DE_SERVICIO")));
        return leyenda;
    }

    private JPanel elementoLeyenda(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JLabel muestra = new JLabel("  ");
        muestra.setOpaque(true);
        muestra.setBackground(color);
        muestra.setBorder(BorderFactory.createLineBorder(color.darker()));
        item.add(muestra);
        item.add(new JLabel(texto));
        return item;
    }

    private JsonNode buscarButaca(List<JsonNode> butacas, String fila, int numero) {
        return butacas.stream().filter(butaca -> butaca.path("fila").asText().equals(fila)
                && butaca.path("numero").asInt() == numero).findFirst().orElse(null);
    }

    private Color colorEstado(String estado) {
        return switch (estado) {
            case "OCUPADA", "BLOQUEADA" -> new Color(105, 110, 118);
            case "FUERA_DE_SERVICIO" -> new Color(190, 52, 52);
            default -> new Color(46, 145, 82);
        };
    }

    private static class PantallaPanel extends JPanel {
        private PantallaPanel() {
            setPreferredSize(new Dimension(760, 70));
            setMinimumSize(new Dimension(420, 70));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int ancho = Math.max(240, getWidth() - 160);
            int x = (getWidth() - ancho) / 2;
            g2.setColor(new Color(62, 66, 72));
            g2.fillRoundRect(x, 12, ancho, 34, 22, 22);
            g2.setColor(new Color(225, 225, 225));
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            String texto = "PANTALLA";
            int textoX = (getWidth() - g2.getFontMetrics().stringWidth(texto)) / 2;
            g2.drawString(texto, textoX, 34);
            g2.dispose();
        }
    }
}
