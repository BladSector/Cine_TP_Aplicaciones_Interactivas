package desktop.comun.util;

public final class FormatoSwing {
    private FormatoSwing() {
    }

    public static String nombreFormato(String formato) {
        return switch (formato) {
            case "DOS_D" -> "2D";
            case "TRES_D" -> "3D";
            default -> formato;
        };
    }

    public static String nombreIdioma(String idioma) {
        return "ESPANIOL".equals(idioma) ? "ESPAÑOL" : idioma;
    }

    public static String nombreEstadoSala(String estado) {
        return switch (estado) {
            case "LIMPIEZA" -> "Limpieza";
            case "CERRADA" -> "Cerrada";
            case "PROBLEMA_PARTICULAR", "FUERA_DE_SERVICIO" -> "Problema particular";
            default -> "Disponible";
        };
    }

    public static String nombreEstadoButaca(String estadoButaca) {
        return switch (estadoButaca) {
            case "OCUPADA" -> "Ocupada";
            case "BLOQUEADA" -> "No disponible";
            case "FUERA_DE_SERVICIO" -> "Fuera de servicio";
            default -> "Disponible";
        };
    }

    public static String nombreEstadoTicket(String estado) {
        return switch (estado) {
            case "PENDIENTE_DE_INGRESO" -> "Pendiente de ingreso";
            case "CONSUMOS_PENDIENTES" -> "Entradas validadas, consumos pendientes";
            case "PROCESADO" -> "Procesado";
            case "REEMBOLSADO" -> "Reembolsado";
            default -> estado;
        };
    }

    public static String codigoFormato(String formato) {
        return "3D".equals(formato) ? "TRES_D" : "DOS_D";
    }

    public static String codigoIdioma(String idioma) {
        return "ESPAÑOL".equals(idioma) ? "ESPANIOL" : idioma;
    }

    public static String codigoEstadoSala(String estado) {
        return switch (estado) {
            case "Limpieza" -> "LIMPIEZA";
            case "Cerrada" -> "CERRADA";
            case "Problema particular" -> "PROBLEMA_PARTICULAR";
            default -> "DISPONIBLE";
        };
    }
}
