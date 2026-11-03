package desktop.empleado.modulos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.databind.JsonNode;

import desktop.comun.ApiClient;
import desktop.comun.api.SalasApi;
import desktop.comun.modulos.ModuloSwing;

import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.tabla;
import static desktop.comun.util.FormatoSwing.nombreEstadoSala;

public final class AvisosModulo extends ModuloSwing {
    private final SalasApi salasApi;
    private final JButton avisosPendientes = new JButton("Avisos");
    private Timer temporizadorAvisos;
    private boolean consultandoAvisos;
    private boolean mostrandoAvisos;

    public AvisosModulo(ApiClient apiClient, Component padre, Consumer<String> estado) {
        super(padre, estado);
        this.salasApi = new SalasApi(apiClient);
        avisosPendientes.addActionListener(event -> mostrarHistorialAvisos());
        iniciarMonitoreoAvisos();
    }

    public JButton boton() {
        return avisosPendientes;
    }

    @Override
    public void recargar() {
        consultarAvisos(false);
    }

    @Override
    public void detener() {
        super.detener();
        detenerMonitoreoAvisos();
    }

    private void iniciarMonitoreoAvisos() {
        temporizadorAvisos = new Timer(15000, event -> consultarAvisos(false));
        temporizadorAvisos.setInitialDelay(1200);
        temporizadorAvisos.start();
    }

    private void mostrarHistorialAvisos() {
        if (consultandoAvisos || mostrandoAvisos || !estaActivo()) {
            return;
        }
        consultandoAvisos = true;
        actualizarEstado("Cargando historial de avisos...");
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                return salasApi.listarAvisos();
            }

            @Override
            protected void done() {
                if (!estaActivo()) {
                    return;
                }
                consultandoAvisos = false;
                try {
                    JsonNode avisos = get();
                    mostrarDialogoHistorialAvisos(avisos);
                    actualizarEstado("Historial de avisos actualizado.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    actualizarEstado("Se interrumpió la carga de avisos.");
                } catch (ExecutionException e) {
                    actualizarEstado("No se pudo cargar el historial de avisos.");
                    JOptionPane.showMessageDialog(
                            padre,
                            e.getCause().getMessage(),
                            "No se pudieron cargar los avisos",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void mostrarDialogoHistorialAvisos(JsonNode avisos) {
        if (avisos.isEmpty()) {
            JOptionPane.showMessageDialog(
                    padre,
                    "Todavía no tenés avisos.",
                    "Historial de avisos",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        DefaultTableModel historialModel = modelo(
                "Estado", "Fecha y hora", "Sala", "Situación", "Descripción", "Informado por"
        );
        avisos.forEach(aviso -> historialModel.addRow(new Object[]{
                aviso.path("leido").asBoolean() ? "Leído" : "Nuevo",
                aviso.path("fechaHora").asText().replace('T', ' '),
                aviso.path("salaNombre").asText(),
                nombreEstadoSala(aviso.path("estado").asText()),
                aviso.path("descripcion").asText(),
                aviso.path("creadoPorUsuario").asText()
        }));

        JTable historial = tabla(historialModel);
        historial.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JScrollPane desplazamiento = new JScrollPane(historial);
        desplazamiento.setPreferredSize(new Dimension(900, 360));
        JOptionPane.showMessageDialog(
                padre,
                desplazamiento,
                "Historial de avisos",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void consultarAvisos(boolean informarSiNoHay) {
        if (consultandoAvisos || mostrandoAvisos || !estaActivo()) {
            return;
        }
        consultandoAvisos = true;
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                return salasApi.listarAvisosPendientes();
            }

            @Override
            protected void done() {
                if (!estaActivo()) {
                    return;
                }
                consultandoAvisos = false;
                try {
                    JsonNode avisos = get();
                    int cantidad = avisos.size();
                    avisosPendientes.setText(cantidad == 0 ? "Avisos" : "Avisos (" + cantidad + ")");
                    avisosPendientes.setForeground(cantidad == 0 ? null : new Color(178, 45, 45));
                    if (cantidad > 0) {
                        mostrarAvisos(avisos);
                    } else if (informarSiNoHay) {
                        JOptionPane.showMessageDialog(
                                padre,
                                "No tenés avisos pendientes.",
                                "Avisos",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    if (informarSiNoHay) {
                        JOptionPane.showMessageDialog(
                                padre,
                                e.getCause().getMessage(),
                                "No se pudieron cargar los avisos",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        }.execute();
    }

    private void mostrarAvisos(JsonNode avisos) {
        mostrandoAvisos = true;
        StringBuilder mensaje = new StringBuilder();
        avisos.forEach(aviso -> mensaje
                .append("Sala: ").append(aviso.path("salaNombre").asText()).append('\n')
                .append("Estado: ").append(nombreEstadoSala(aviso.path("estado").asText())).append('\n')
                .append("Descripción: ").append(aviso.path("descripcion").asText()).append('\n')
                .append("Informado por: ").append(aviso.path("creadoPorUsuario").asText()).append('\n')
                .append("Fecha: ").append(aviso.path("fechaHora").asText().replace('T', ' '))
                .append("\n\n"));

        JTextArea detalle = new JTextArea(mensaje.toString(), 14, 48);
        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(
                padre,
                new JScrollPane(detalle),
                avisos.size() == 1 ? "Nuevo aviso de sala" : "Nuevos avisos de sala",
                JOptionPane.WARNING_MESSAGE
        );

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (JsonNode aviso : avisos) {
                    salasApi.marcarAvisoLeido(aviso.path("id").asLong());
                }
                return null;
            }

            @Override
            protected void done() {
                if (!estaActivo()) {
                    return;
                }
                mostrandoAvisos = false;
                try {
                    get();
                    avisosPendientes.setText("Avisos");
                    avisosPendientes.setForeground(null);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ignored) {
                    avisosPendientes.setText("Avisos pendientes");
                }
            }
        }.execute();
    }

    private void detenerMonitoreoAvisos() {
        if (temporizadorAvisos != null) {
            temporizadorAvisos.stop();
        }
    }

}
