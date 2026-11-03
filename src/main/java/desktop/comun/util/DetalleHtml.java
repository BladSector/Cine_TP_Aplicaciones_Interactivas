package desktop.comun.util;

public final class DetalleHtml {
    private DetalleHtml() {}

    public static String inicioHtml() {
        return "<html><body style='font-family:sans-serif;font-size:10px;margin:8px'>";
    }

    public static String mensajeHtml(String mensaje) {
        return inicioHtml() + escaparHtml(mensaje) + "</body></html>";
    }

    public static String campoHtml(String etiqueta, Object valor) {
        String contenido = escaparHtml(valor == null || valor.toString().isBlank() ? "-" : valor)
                .replace("\n", "<br>");
        return "<table width='100%' cellpadding='4' cellspacing='0'>"
                + "<tr><td width='38%'><b>" + escaparHtml(etiqueta) + ":</b></td>"
                + "<td>" + contenido + "</td></tr></table>";
    }

    public static String tituloHtml(String titulo) {
        return "<div align='center'><h2>" + escaparHtml(titulo) + "</h2></div><hr>";
    }

    public static String seccionHtml(String titulo) {
        return "<br><br><h3>" + escaparHtml(titulo) + "</h3><hr><br>";
    }

    public static String itemHtml(Object titulo) {
        return "<p><b>" + escaparHtml(titulo) + "</b></p>";
    }

    public static String mensajeSecundarioHtml(String mensaje) {
        return "<p><i>" + escaparHtml(mensaje) + "</i></p>";
    }

    public static String escaparHtml(Object valor) {
        return String.valueOf(valor)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
