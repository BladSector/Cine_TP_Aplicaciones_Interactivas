package desktop.comun.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextoBusqueda {
    private TextoBusqueda() {
    }

    public static String normalizarBusqueda(String texto) {
        String normalizado = Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
