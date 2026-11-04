package desktop.comun.modulos;

import desktop.comun.componentes.VisorDetalle;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.mensajeHtml;
import static desktop.comun.util.DetalleHtml.campoHtml;
import com.fasterxml.jackson.databind.JsonNode;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ModuloSwing {
    protected final Component padre;
    private final Consumer<String> estadoExterno;
    private boolean activo = true;
    private final Map<String, Long> versionesConsultas = new HashMap<>();

    protected ModuloSwing(Component padre, Consumer<String> estadoExterno) {
        this.padre = padre;
        this.estadoExterno = estadoExterno == null ? mensaje -> {} : estadoExterno;
    }

    public abstract void recargar();

    public void detener() {
        activo = false;
    }

    protected boolean estaActivo() {
        return activo;
    }

    protected void instalarDetalleDobleClick(JTable tabla, String titulo, Supplier<String> descripcion) {
        instalarDetalleDobleClick(tabla, titulo, descripcion, null, null);
    }

    protected void instalarDetalleDobleClick(JTable tabla, String titulo, Supplier<String> descripcion,
                                            String accionTexto, Runnable accion) {
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() != 2 || event.getButton() != java.awt.event.MouseEvent.BUTTON1) {
                    return;
                }
                int fila = tabla.rowAtPoint(event.getPoint());
                if (fila >= 0) {
                    tabla.setRowSelectionInterval(fila, fila);
                    if (accion == null) {
                        mostrarDetalle(titulo, descripcion.get());
                    } else {
                        mostrarDetalleConAccion(titulo, descripcion.get(), accionTexto, accion);
                    }
                }
            }
        });
    }

    protected String descripcionFila(JTable tabla) {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná un registro.");
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        StringBuilder descripcion = new StringBuilder(inicioHtml());
        for (int columna = 0; columna < tabla.getModel().getColumnCount(); columna++) {
            Object valor = tabla.getModel().getValueAt(fila, columna);
            descripcion.append(campoHtml(
                    tabla.getModel().getColumnName(columna),
                    valor == null || valor.toString().isBlank() ? "-" : valor
            ));
        }
        return descripcion.append("</body></html>").toString();
    }

    protected void mostrarDetalle(String titulo, String descripcion) {
        mostrarDetalleConAccion(titulo, descripcion, null, null);
    }

    protected void mostrarDetalleConAccion(String titulo, String descripcion, String accionTexto, Runnable accion) {
        JEditorPane area = new VisorDetalle("");
        area.setText(descripcion);
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(440, 300));
        if (accion == null) {
            JOptionPane.showMessageDialog(padre, scroll, titulo, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object[] opciones = {"Cerrar", accionTexto};
        int opcion = JOptionPane.showOptionDialog(
                padre, scroll, titulo, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, opciones, opciones[0]
        );
        if (opcion == 1) {
            accion.run();
        }
    }

    protected Integer idSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Seleccioná primero un registro de la tabla.");
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return ((Number) tabla.getModel().getValueAt(filaModelo, 0)).intValue();
    }

    protected String textoNullable(JsonNode nodo) {
        return nodo == null || nodo.isNull() ? "-" : nodo.asText();
    }

    protected void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    protected void actualizarEstado(String mensaje) {
        if (activo) {
            estadoExterno.accept(mensaje);
        }
    }

    protected <T> void consultar(String clave, String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        long version = versionesConsultas.merge(clave, 1L, Long::sum);
        ejecutar(mensaje, operacion, alCompletar, () -> versionesConsultas.get(clave) == version);
    }

    protected <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar) {
        ejecutar(mensaje, operacion, alCompletar, () -> true);
    }

    private <T> void ejecutar(String mensaje, Operacion<T> operacion, Consumer<T> alCompletar,
                             BooleanSupplier vigente) {
        if (!activo) {
            return;
        }
        actualizarEstado(mensaje);
        // Las llamadas HTTP se ejecutan fuera del hilo gráfico para no congelar Swing.
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                if (!activo || !vigente.getAsBoolean()) {
                    return;
                }
                try {
                    T resultado = get();
                    alCompletar.accept(resultado);
                    actualizarEstado("Operación completada");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    actualizarEstado("Operación interrumpida");
                } catch (ExecutionException e) {
                    actualizarEstado("La operación falló");
                    JOptionPane.showMessageDialog(padre,
                            e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    @FunctionalInterface
    protected interface Operacion<T> {
        T ejecutar() throws Exception;
    }
}
