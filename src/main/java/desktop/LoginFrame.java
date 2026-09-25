package desktop;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LoginFrame extends JFrame {
    private final ApiClient apiClient;
    private final JTextField usuario = new JTextField(18);
    private final JPasswordField contrasenia = new JPasswordField(18);
    private final JButton ingresar = new JButton("Ingresar");
    private final JLabel estado = new JLabel(" ");

    public LoginFrame(ApiClient apiClient) {
        super("Cine API - Personal");
        this.apiClient = apiClient;
        construirVista();
    }

    private void construirVista() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(430, 285));
        setLocationRelativeTo(null);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(28, 36, 24, 36));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 24, 0);
        JLabel titulo = new JLabel("Acceso del personal del cine");
        titulo.setFont(titulo.getFont().deriveFont(20f));
        formulario.add(titulo, gbc);

        agregarCampo(formulario, gbc, 1, "Usuario", usuario);
        agregarCampo(formulario, gbc, 2, "Contraseña", contrasenia);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(18, 8, 8, 0);
        formulario.add(ingresar, gbc);

        estado.setBorder(BorderFactory.createEmptyBorder(0, 36, 18, 36));
        add(formulario, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        ingresar.addActionListener(event -> iniciarSesion());
        contrasenia.addActionListener(event -> iniciarSesion());
        pack();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta,
                              java.awt.Component componente) {
        gbc.gridwidth = 1;
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 0, 8, 12);
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(componente, gbc);
    }

    private void iniciarSesion() {
        String nombreUsuario = usuario.getText().trim();
        String clave = new String(contrasenia.getPassword());
        if (nombreUsuario.isBlank() || clave.isBlank()) {
            JOptionPane.showMessageDialog(this, "Completá el usuario y la contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ingresar.setEnabled(false);
        estado.setText("Conectando con la API...");
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                apiClient.post("/sesion/personal", Map.of("usuario", nombreUsuario, "contrasenia", clave));
                return apiClient.get("/sesion/personal");
            }

            @Override
            protected void done() {
                ingresar.setEnabled(true);
                try {
                    JsonNode respuesta = get();
                    String rol = respuesta.path("rol").asText("EMPLEADO");
                    if ("ADMIN".equals(rol)) {
                        rol = "DUENIO";
                    }
                    dispose();
                    new PrincipalFrame(apiClient, rol, respuesta.path("usuario").asText(nombreUsuario)).setVisible(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("La operación fue interrumpida.");
                } catch (ExecutionException e) {
                    mostrarError(e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void mostrarError(String mensaje) {
        estado.setText("No se pudo iniciar sesión.");
        JOptionPane.showMessageDialog(this, mensaje, "Error de acceso", JOptionPane.ERROR_MESSAGE);
    }
}
