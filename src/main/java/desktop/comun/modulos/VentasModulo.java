package desktop.comun.modulos;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.fasterxml.jackson.databind.JsonNode;

import desktop.comun.ApiClient;
import desktop.comun.api.TicketsApi;
import desktop.comun.componentes.PanelConTabla;
import desktop.comun.paneles.ConsumosPanel;
import desktop.comun.paneles.TicketsPanel;

import static desktop.comun.util.ConfiguradorTablas.limpiar;
import static desktop.comun.util.ConfiguradorTablas.modelo;
import static desktop.comun.util.ConfiguradorTablas.tabla;
import static desktop.comun.util.DetalleHtml.campoHtml;
import static desktop.comun.util.DetalleHtml.inicioHtml;
import static desktop.comun.util.DetalleHtml.itemHtml;
import static desktop.comun.util.DetalleHtml.mensajeHtml;
import static desktop.comun.util.DetalleHtml.mensajeSecundarioHtml;
import static desktop.comun.util.DetalleHtml.seccionHtml;
import static desktop.comun.util.DetalleHtml.tituloHtml;
import static desktop.comun.util.FormatoSwing.nombreEstadoTicket;

public final class VentasModulo extends ModuloSwing {
    private final TicketsApi ticketsApi;
    private final boolean empleado;
    private final DefaultTableModel consumosModel = modelo("ID", "Producto", "Cantidad", "Ticket", "Estado", "Subtotal");
    private final DefaultTableModel ticketsModel = modelo("ID", "Espectador", "Película", "Fecha", "Horario", "Sala",
            "Estado", "Entradas", "Consumos", "Total", "Código QR");
    private final JTable consumosTabla = tabla(consumosModel);
    private final JTable ticketsTabla = tabla(ticketsModel);
    private final JTextField codigoQr = new JTextField(32);
    private final JTextField codigoQrConsumo = new JTextField(28);
    private final Map<Integer, JsonNode> ticketsActuales = new LinkedHashMap<>();
    private Integer ticketActualId;
    private final JPanel ticketsPanel;
    private final JPanel consumosPanel;

    public VentasModulo(ApiClient apiClient, boolean empleado, Component padre, Consumer<String> estado) {
        super(padre, estado);
        this.ticketsApi = new TicketsApi(apiClient);
        this.empleado = empleado;
        ticketsPanel = crearPanelTickets();
        consumosPanel = crearPanelConsumos();
        instalarDetalleDobleClick(consumosTabla, "Información del consumo",
                () -> descripcionFila(consumosTabla), "Marcar entregado", this::entregarConsumoSeleccionado);
        instalarDetalleDobleClick(ticketsTabla, "Información del ticket", this::descripcionTicketSeleccionado);
    }

    public JPanel panelTickets() {
        return ticketsPanel;
    }
    public JPanel panelConsumos() {
        return consumosPanel;
    }

    @Override
    public void recargar() {
        cargarTickets();
        cargarConsumos();
    }

    private boolean esEmpleado() {
        return empleado;
    }

    private JPanel crearPanelTickets() {
        return new TicketsPanel(
                esEmpleado(),
                ticketsTabla,
                new PanelConTabla("Tickets creados", ticketsTabla),
                codigoQr,
                ticketId -> ticketActualId = ticketId,
                this::leerQrAcceso,
                this::validarEntradasManualmente,
                this::reembolsarTicket,
                this::cargarTickets
        );
    }

    private JPanel crearPanelConsumos() {
        return new ConsumosPanel(
                new PanelConTabla("Consumos pendientes y entregados", consumosTabla),
                codigoQrConsumo,
                this::entregarConsumosPorQr,
                this::entregarConsumosManualmente,
                this::cargarConsumos
        );
    }

    private String descripcionTicketSeleccionado() {
        int filaVista = ticketsTabla.getSelectedRow();
        if (filaVista < 0) {
            return mensajeHtml("Seleccioná un ticket para ver su información.");
        }
        int fila = ticketsTabla.convertRowIndexToModel(filaVista);
        int id = ((Number) ticketsModel.getValueAt(fila, 0)).intValue();
        JsonNode ticket = ticketsActuales.get(id);
        if (ticket == null) {
            return mensajeHtml("No se encontró la información del ticket.");
        }

        StringBuilder descripcion = new StringBuilder(inicioHtml());
        descripcion.append(tituloHtml("TICKET #" + id))
                .append(campoHtml("Estado", nombreEstadoTicket(ticket.path("estado").asText())))
                .append(campoHtml("Espectador", ticket.path("espectadorNombre").asText()))
                .append(campoHtml("Código QR", ticket.path("codigoQR").asText()))
                .append(campoHtml("Método de pago", ticket.path("metodoDePagoResumen").asText("-")))
                .append(campoHtml("Total", "$" + ticket.path("total").asDouble()))
                .append(seccionHtml("ENTRADAS"));

        if (ticket.path("entradas").isEmpty()) {
            descripcion.append(mensajeSecundarioHtml("El ticket no contiene entradas."));
        } else {
            ticket.path("entradas").forEach(entrada -> descripcion
                    .append(itemHtml(entrada.path("pelicula").asText()))
                    .append(campoHtml("Función", entrada.path("fecha").asText()
                            + " " + entrada.path("horario").asText()))
                    .append(campoHtml("Sala y butaca", entrada.path("sala").asText()
                            + " - " + entrada.path("butaca").asText()))
                    .append(campoHtml("Estado", entrada.path("estado").asText()))
                    .append("<hr>"));
        }

        descripcion.append(seccionHtml("CONSUMOS"));
        if (ticket.path("itemsConsumo").isEmpty()) {
            descripcion.append(mensajeSecundarioHtml("El ticket no contiene consumos."));
        } else {
            ticket.path("itemsConsumo").forEach(item -> descripcion
                    .append(itemHtml(item.path("producto").asText()))
                    .append(campoHtml("Cantidad", item.path("cantidad").asInt()))
                    .append(campoHtml("Estado", item.path("estado").asText()))
                    .append("<hr>"));
        }
        return descripcion.append("</body></html>").toString();
    }

    private void entregarConsumoSeleccionado() {
        int filaVista = consumosTabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarAviso("Seleccioná primero un consumo.");
            return;
        }
        int fila = consumosTabla.convertRowIndexToModel(filaVista);
        if (!"PENDIENTE".equals(String.valueOf(consumosModel.getValueAt(fila, 4)))) {
            mostrarAviso("Solo se puede entregar un consumo pendiente.");
            return;
        }
        operarConsumo();
    }

    private void cargarConsumos() {
        ejecutar("Cargando consumos...", ticketsApi::listarConsumos, respuesta -> {
            limpiar(consumosModel);
            respuesta.forEach(item -> consumosModel.addRow(new Object[]{
                    item.path("id").asInt(), item.path("productoNombre").asText(),
                    item.path("cantidad").asInt(), textoNullable(item.get("ticketId")),
                    item.path("estado").asText(), item.path("subtotal").asDouble()
            }));
        });
    }

    private void cargarTickets() {
        ejecutar("Cargando tickets...", ticketsApi::listarTickets, respuesta -> {
            limpiar(ticketsModel);
            ticketsActuales.clear();
            respuesta.forEach(ticket -> {
                int id = ticket.path("id").asInt();
                ticketsActuales.put(id, ticket);
                ticketsModel.addRow(new Object[]{
                        id,
                        ticket.path("espectadorNombre").asText(),
                        valoresEntradas(ticket, "pelicula"),
                        valoresEntradas(ticket, "fecha"),
                        valoresEntradas(ticket, "horario"),
                        valoresEntradas(ticket, "sala"),
                        nombreEstadoTicket(ticket.path("estado").asText()),
                        ticket.path("entradas").size(),
                        ticket.path("itemsConsumo").size(),
                        ticket.path("total").asDouble(),
                        ticket.path("codigoQR").asText()
                });
            });
        });
    }

    private static String valoresEntradas(JsonNode ticket, String campo) {
        List<String> valores = new ArrayList<>();
        ticket.path("entradas").forEach(entrada -> {
            String valor = entrada.path(campo).asText("").trim();
            if (!valor.isEmpty() && !valores.contains(valor)) {
                valores.add(valor);
            }
        });
        return valores.isEmpty() ? "-" : String.join(", ", valores);
    }

    private void leerQrAcceso() {
        String codigo = codigoQr.getText().trim();
        if (codigo.isBlank()) {
            mostrarAviso("Ingresá el código del Ticket.");
            return;
        }
        ejecutar("Consultando ticket...",
                () -> ticketsApi.buscarPorQr(codigo),
                ticket -> confirmarValidacionEntradas(ticket, true));
    }

    private void entregarConsumosPorQr() {
        String codigo = codigoQrConsumo.getText().trim();
        if (codigo.isBlank()) {
            mostrarAviso("Ingresá el código QR del Ticket.");
            return;
        }
        ejecutar("Consultando ticket...",
                () -> ticketsApi.buscarPorQr(codigo),
                ticket -> entregarConsumosTicket(ticket.path("id").asInt(), true));
    }

    private void validarEntradasManualmente() {
        if (ticketActualId == null) {
            mostrarAviso("Seleccioná un ticket de la tabla para validar sus entradas manualmente.");
            return;
        }

        int ticketId = ticketActualId;
        ejecutar("Consultando ticket...",
                () -> ticketsApi.buscarPorId(ticketId),
                ticket -> confirmarValidacionEntradas(ticket, false));
    }

    private void confirmarValidacionEntradas(JsonNode ticket, boolean lecturaQr) {
        int consumosPendientes = cantidadConsumosPendientes(ticket);

        if (consumosPendientes > 0) {
            int confirmacion = JOptionPane.showConfirmDialog(
                    padre,
                    "El Ticket #" + ticket.path("id").asInt() + " todavía tiene "
                            + consumosPendientes + " consumo(s) pendiente(s) de entrega.\n"
                            + "Si validás el acceso ahora, los consumos seguirán pendientes.\n\n"
                            + "¿Querés continuar igualmente?",
                    "Consumos pendientes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        } else if (!lecturaQr) {
            int confirmacion = JOptionPane.showConfirmDialog(
                    padre,
                    "¿Validar manualmente las entradas del Ticket #"
                            + ticket.path("id").asInt() + "?",
                    "Validación manual",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        validarEntradasTicket(ticket.path("id").asInt(), lecturaQr);
    }

    private int cantidadConsumosPendientes(JsonNode ticket) {
        int cantidad = 0;
        JsonNode items = ticket.path("itemsConsumo");
        if (!items.isArray()) {
            return cantidad;
        }

        for (JsonNode item : items) {
            if ("PENDIENTE".equals(item.path("estado").asText())) {
                cantidad += Math.max(1, item.path("cantidad").asInt());
            }
        }
        return cantidad;
    }

    private void validarEntradasTicket(int ticketId, boolean lecturaQr) {
        ejecutar("Validando acceso...",
                () -> ticketsApi.validarEntradas(ticketId),
                ticket -> {
                    cargarTickets();
                    cargarConsumos();
                    JOptionPane.showMessageDialog(
                            padre,
                            "Las entradas del Ticket #" + ticket.path("id").asInt()
                                    + " fueron validadas. Los consumos no se modificaron.",
                            "Acceso validado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (lecturaQr) {
                        codigoQr.requestFocusInWindow();
                        codigoQr.selectAll();
                    }
                });
    }

    private void entregarConsumosManualmente() {
        int filaVista = consumosTabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarAviso("Seleccioná un consumo de la tabla para realizar la entrega manual.");
            return;
        }

        int fila = consumosTabla.convertRowIndexToModel(filaVista);
        String estadoConsumo = String.valueOf(consumosModel.getValueAt(fila, 4));
        if (!"PENDIENTE".equals(estadoConsumo)) {
            mostrarAviso("El consumo seleccionado no está pendiente de entrega.");
            return;
        }

        Object valorTicketId = consumosModel.getValueAt(fila, 3);
        try {
            int ticketId = Integer.parseInt(String.valueOf(valorTicketId));
            confirmarEntregaManual(ticketId);
        } catch (NumberFormatException e) {
            mostrarAviso("El consumo seleccionado no tiene un ticket asociado.");
        }
    }

    private void confirmarEntregaManual(int ticketId) {
        int confirmacion = JOptionPane.showConfirmDialog(
                padre,
                "Se marcarán como entregados todos los consumos pendientes del Ticket #"
                        + ticketId + ". ¿Continuar?",
                "Entrega manual",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirmacion == JOptionPane.YES_OPTION) {
            entregarConsumosTicket(ticketId, false);
        }
    }

    private void entregarConsumosTicket(int ticketId, boolean lecturaQr) {
        ejecutar("Entregando consumos...",
                () -> ticketsApi.entregarConsumos(ticketId),
                ticket -> {
                    cargarConsumos();
                    cargarTickets();
                    JOptionPane.showMessageDialog(
                            padre,
                            "Los consumos del Ticket #" + ticket.path("id").asInt()
                                    + " fueron marcados como entregados.",
                            "Consumos entregados",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (lecturaQr) {
                        codigoQrConsumo.requestFocusInWindow();
                        codigoQrConsumo.selectAll();
                    }
                });
    }

    private void reembolsarTicket() {
        if (ticketActualId == null) {
            mostrarAviso("Seleccioná un ticket de la tabla para reembolsarlo.");
            return;
        }
        int ticketId = ticketActualId;
        int confirmacion = JOptionPane.showConfirmDialog(
                padre,
                "Se reembolsará el Ticket #" + ticketId
                        + ", se liberarán sus butacas y se cancelarán los consumos pendientes. ¿Continuar?",
                "Reembolsar ticket",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutar("Reembolsando ticket...",
                () -> ticketsApi.reembolsarTicket(ticketId),
                respuesta -> {
                    cargarTickets();
                    cargarConsumos();
                    JOptionPane.showMessageDialog(
                            padre,
                            "El ticket fue reembolsado correctamente.",
                            "Reembolso completado",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                });
    }

    private void operarConsumo() {
        Integer id = idSeleccionado(consumosTabla);
        if (id == null) {
            return;
        }
        ejecutar("Actualizando consumo...",
                () -> ticketsApi.entregarConsumo(id),
                respuesta -> cargarConsumos());
    }
}
