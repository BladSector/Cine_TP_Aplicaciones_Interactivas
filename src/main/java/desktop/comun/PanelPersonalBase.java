package desktop.comun;

import desktop.comun.modulos.ModuloSwing;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class PanelPersonalBase extends JPanel {
    private final CardLayout navegacion = new CardLayout();
    private final JPanel vistas = new JPanel(navegacion);
    private final JPanel menu = new JPanel();
    private final Map<String, JButton> botones = new LinkedHashMap<>();
    private final List<Runnable> recargas = new ArrayList<>();
    private final List<Runnable> detenciones = new ArrayList<>();
    private final int alturaBoton;
    private final int espacioBoton;
    private boolean detenido;

    protected PanelPersonalBase(boolean duenio) {
        super(new BorderLayout());
        alturaBoton = duenio ? 38 : 42;
        espacioBoton = duenio ? 4 : 6;
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        int margen = duenio ? 12 : 14;
        menu.setBorder(BorderFactory.createEmptyBorder(margen, 10, margen, 10));
        menu.setPreferredSize(new Dimension(duenio ? 215 : 190, 0));
        JLabel titulo = new JLabel(duenio ? "Gestión del cine" : "Tareas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        menu.add(titulo);
        menu.add(Box.createVerticalStrut(duenio ? 12 : 14));
        if (duenio) {
            JScrollPane scroll = new JScrollPane(menu);
            scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(205, 205, 205)));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(14);
            add(scroll, BorderLayout.WEST);
        } else {
            add(menu, BorderLayout.WEST);
        }
        add(vistas, BorderLayout.CENTER);
    }

    protected final void agregarGrupo(String nombre) {
        if (menu.getComponentCount() > 2) {
            menu.add(Box.createVerticalStrut(9));
        }
        JLabel grupo = new JLabel(nombre);
        grupo.setFont(grupo.getFont().deriveFont(Font.BOLD, 11f));
        grupo.setForeground(new Color(95, 95, 95));
        grupo.setAlignmentX(Component.LEFT_ALIGNMENT);
        grupo.setBorder(BorderFactory.createEmptyBorder(0, 7, 4, 0));
        menu.add(grupo);
    }

    protected final void agregarSeccion(String nombre, JPanel vista) {
        JButton boton = new JButton(nombre);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, alturaBoton));
        boton.setFocusPainted(false);
        boton.addActionListener(event -> mostrarSeccion(nombre));
        botones.put(nombre, boton);
        vistas.add(vista, nombre);
        menu.add(boton);
        menu.add(Box.createVerticalStrut(espacioBoton));
    }

    protected final void finalizarMenu(String seccionInicial) {
        menu.add(Box.createVerticalGlue());
        mostrarSeccion(seccionInicial);
    }

    protected final void registrarModulo(ModuloSwing modulo) {
        registrarCicloDeVida(modulo::recargar, modulo::detener);
    }

    protected final void registrarCicloDeVida(Runnable recargar, Runnable detener) {
        recargas.add(recargar);
        detenciones.add(detener);
    }

    public final List<String> secciones() {
        return new ArrayList<>(botones.keySet());
    }

    public final void mostrarSeccion(String nombre) {
        navegacion.show(vistas, nombre);
        botones.forEach((seccion, boton) -> {
            boolean seleccionada = seccion.equals(nombre);
            boton.setFont(boton.getFont().deriveFont(seleccionada ? Font.BOLD : Font.PLAIN));
            boton.setBackground(seleccionada ? new Color(214, 225, 241) : null);
            boton.setOpaque(seleccionada);
        });
    }

    public JButton botonAvisos() {
        return null;
    }

    public final void recargar() {
        if (!detenido) {
            recargas.forEach(Runnable::run);
        }
    }

    public final void detener() {
        if (!detenido) {
            detenido = true;
            detenciones.forEach(Runnable::run);
        }
    }
}
