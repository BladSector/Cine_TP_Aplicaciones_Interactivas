package desktop.comun;

import desktop.duenio.DuenioPanel;
import desktop.comun.api.SesionApi;
import desktop.empleado.EmpleadoPanel;
import desktop.inicio.LoginFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PrincipalFrame extends JFrame {
    private final ApiClient apiClient;
    private final SesionApi sesionApi;
    private final String rol;
    private final JLabel estado = new JLabel("Listo");
    private final PanelPersonalBase panelPersonal;

    public PrincipalFrame(ApiClient apiClient, String rol, String usuario) {
        super("Cine API - " + nombreRol(rol));
        this.apiClient = apiClient;
        this.sesionApi = new SesionApi(apiClient);
        this.rol = rol;
        this.panelPersonal = crearPanelPersonal();
        construirVista(usuario);
    }

    private PanelPersonalBase crearPanelPersonal() {
        if (esDuenio()) {
            return new DuenioPanel(apiClient, estado::setText);
        }
        return new EmpleadoPanel(apiClient, estado::setText);
    }

    private void construirVista(String usuario) {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setSize(1220, 790);
        setLocationRelativeTo(null);

        add(crearCabecera(usuario), BorderLayout.NORTH);
        add(panelPersonal, BorderLayout.CENTER);

        estado.setBorder(BorderFactory.createEmptyBorder(7, 14, 9, 14));
        estado.setOpaque(true);
        estado.setBackground(new Color(238, 238, 238));
        add(estado, BorderLayout.SOUTH);

        setJMenuBar(crearMenu());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                salirAplicacion();
            }
        });
    }

    private JPanel crearCabecera(String usuario) {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titulo = new JLabel(esDuenio() ? "Administración del cine" : "Operación del cine");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));

        JToolBar herramientas = new JToolBar();
        herramientas.setFloatable(false);
        herramientas.setBorderPainted(false);
        herramientas.add(new JLabel(usuario + " | " + nombreRol(rol)));
        herramientas.addSeparator();
        if (esEmpleado()) {
            herramientas.add(panelPersonal.botonAvisos());
        }

        JButton recargar = new JButton("Recargar");
        recargar.addActionListener(event -> panelPersonal.recargar());
        herramientas.add(recargar);

        JButton cerrarSesion = new JButton("Cerrar sesión");
        cerrarSesion.addActionListener(event -> cerrarSesion());
        herramientas.add(cerrarSesion);

        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(herramientas, BorderLayout.EAST);
        return cabecera;
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        JMenuItem cerrar = new JMenuItem("Cerrar sesión");
        cerrar.addActionListener(event -> cerrarSesion());
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(event -> salirAplicacion());
        archivo.add(cerrar);
        archivo.addSeparator();
        archivo.add(salir);

        JMenu operacion = new JMenu("Operación");
        panelPersonal.secciones().forEach(nombre -> operacion.add(itemMenu(nombre)));
        operacion.addSeparator();
        JMenuItem recargar = new JMenuItem("Recargar datos");
        recargar.addActionListener(event -> panelPersonal.recargar());
        operacion.add(recargar);

        barra.add(archivo);
        barra.add(operacion);
        return barra;
    }

    private JMenuItem itemMenu(String nombre) {
        JMenuItem item = new JMenuItem(nombre);
        item.addActionListener(event -> panelPersonal.mostrarSeccion(nombre));
        return item;
    }

    private void cerrarSesion() {
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Querés cerrar la sesión?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutar("Cerrando sesión...", () -> {
            sesionApi.cerrarSesion();
            return null;
        }, respuesta -> {
            panelPersonal.detener();
            dispose();
            new LoginFrame(apiClient).setVisible(true);
        });
    }

    private void salirAplicacion() {
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Querés salir de la aplicación?",
                "Salir",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        panelPersonal.detener();
        dispose();
        System.exit(0);
    }

    private <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        estado.setText(mensaje);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                try {
                    alCompletar.accept(get());
                    estado.setText("Operación completada");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    estado.setText("Operación interrumpida");
                } catch (ExecutionException exception) {
                    estado.setText("La operación falló");
                    JOptionPane.showMessageDialog(
                            PrincipalFrame.this,
                            exception.getCause().getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private static String nombreRol(String rol) {
        return switch (rol) {
            case "DUENIO", "ADMIN" -> "Dueño";
            case "EMPLEADO" -> "Empleado";
            default -> "Personal";
        };
    }

    private boolean esDuenio() {
        return "DUENIO".equals(rol) || "ADMIN".equals(rol);
    }

    private boolean esEmpleado() {
        return "EMPLEADO".equals(rol);
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar() throws Exception;
    }
}
