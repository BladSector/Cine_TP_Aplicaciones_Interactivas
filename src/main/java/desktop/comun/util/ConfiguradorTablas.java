package desktop.comun.util;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public final class ConfiguradorTablas {
    private ConfiguradorTablas() {
    }

    public static JTable tabla(DefaultTableModel model) {
        JTable tabla = crearTabla(model);
        ordenarColumnas(tabla);
        configurarAnchos(tabla);
        return tabla;
    }

    public static JTable tablaAdministracion(DefaultTableModel model) {
        JTable tabla = crearTabla(model);
        ordenarColumnasAdministracion(tabla);
        configurarAnchosAdministracion(tabla);
        return tabla;
    }

    private static JTable crearTabla(DefaultTableModel model) {
        JTable tabla = new JTable(model);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    private static void ordenarColumnas(JTable tabla) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        if (model.findColumn("Fecha y hora") >= 0 && model.findColumn("Acción") >= 0) {
            moverColumnas(tabla, "ID", "Fecha y hora", "Usuario", "Perfil", "Acción",
                    "Entidad", "Entidad ID", "Resultado", "Detalle");
        } else if (model.findColumn("Película") >= 0 && model.findColumn("Fecha") >= 0) {
            moverColumnas(tabla, "ID", "Fecha", "Horario", "Película", "Sala", "Formato", "Idioma", "Precio");
        } else if (model.findColumn("Código QR") >= 0) {
            moverColumnas(tabla, "ID", "Espectador", "Estado", "Entradas", "Consumos", "Total", "Código QR");
        } else if (model.findColumn("Producto") >= 0) {
            moverColumnas(tabla, "ID", "Producto", "Cantidad", "Estado", "Subtotal", "Ticket");
        } else if (model.findColumn("Butaca") >= 0) {
            moverColumnas(tabla, "ID", "Sala", "Butaca", "Estado");
        }
    }

    private static void moverColumnas(JTable tabla, String... nombres) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        int destino = 0;
        for (String nombre : nombres) {
            int indiceModelo = model.findColumn(nombre);
            if (indiceModelo < 0) {
                continue;
            }

            int indiceVista = tabla.convertColumnIndexToView(indiceModelo);
            if (indiceVista >= 0 && indiceVista != destino) {
                tabla.moveColumn(indiceVista, destino);
            }
            destino++;
        }
    }

    private static void configurarAnchos(JTable tabla) {
        for (int columna = 0; columna < tabla.getColumnModel().getColumnCount(); columna++) {
            TableColumn columnaTabla = tabla.getColumnModel().getColumn(columna);
            String nombre = String.valueOf(columnaTabla.getHeaderValue());
            switch (nombre) {
                case "ID" -> configurarColumna(columnaTabla, 42, 50, 65);
                case "Fecha" -> configurarColumna(columnaTabla, 85, 100, 115);
                case "Fecha y hora" -> configurarColumna(columnaTabla, 130, 160, 190);
                case "Horario" -> configurarColumna(columnaTabla, 70, 82, 95);
                case "Capacidad", "Cantidad", "Activo", "Entradas", "Consumos" ->
                        configurarColumna(columnaTabla, 68, 82, 100);
                case "Formato", "Butaca", "Ticket" ->
                        configurarColumna(columnaTabla, 65, 78, 95);
                case "Idioma" -> configurarColumna(columnaTabla, 90, 110, 130);
                case "Precio", "Subtotal", "Total" -> configurarColumna(columnaTabla, 82, 98, 115);
                case "Estado" -> configurarColumna(columnaTabla, 100, 130, 175);
                case "Perfil", "Resultado" -> configurarColumna(columnaTabla, 80, 100, 125);
                case "Entidad ID" -> configurarColumna(columnaTabla, 75, 88, 105);
                case "Película", "Nombre", "Producto", "Espectador" ->
                        configurarColumnaFlexible(columnaTabla, 120, 190);
                case "Código QR" -> configurarColumnaFlexible(columnaTabla, 180, 280);
                case "Sala", "Apellido", "Usuario", "Entidad" ->
                        configurarColumnaFlexible(columnaTabla, 95, 140);
                case "Acción" -> configurarColumnaFlexible(columnaTabla, 140, 190);
                case "Detalle" -> configurarColumnaFlexible(columnaTabla, 180, 280);
                default -> configurarColumnaFlexible(columnaTabla, 80, 120);
            }
        }
    }

    private static void configurarColumna(TableColumn columna, int minimo, int preferido, int maximo) {
        columna.setMinWidth(minimo);
        columna.setPreferredWidth(preferido);
        columna.setMaxWidth(maximo);
    }

    private static void configurarColumnaFlexible(TableColumn columna, int minimo, int preferido) {
        columna.setMinWidth(minimo);
        columna.setPreferredWidth(preferido);
        columna.setMaxWidth(Integer.MAX_VALUE);
    }

    private static void ordenarColumnasAdministracion(JTable tabla) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        if (model.findColumn("Película ID") >= 0) {
            moverColumnas(tabla, "ID", "Fecha", "Horario", "Película", "Sala", "Formato", "Idioma",
                    "Precio", "Película ID", "Sala ID");
        } else if (model.findColumn("Título") >= 0) {
            moverColumnas(tabla, "ID", "Título", "Categoría", "Duración", "Descripción", "Portada");
        } else if (model.findColumn("Tipo") >= 0) {
            moverColumnas(tabla, "ID", "Nombre", "Tipo", "Tamaño", "Precio");
        }
    }

    private static void configurarAnchosAdministracion(JTable tabla) {
        for (int columna = 0; columna < tabla.getColumnModel().getColumnCount(); columna++) {
            TableColumn columnaTabla = tabla.getColumnModel().getColumn(columna);
            String nombre = String.valueOf(columnaTabla.getHeaderValue());
            switch (nombre) {
                case "ID" -> configurarColumna(columnaTabla, 42, 50, 65);
                case "Fecha" -> configurarColumna(columnaTabla, 85, 100, 115);
                case "Horario" -> configurarColumna(columnaTabla, 70, 82, 95);
                case "Duración", "Capacidad", "Cantidad", "Activo" ->
                        configurarColumna(columnaTabla, 68, 82, 100);
                case "Película ID", "Sala ID" -> configurarColumna(columnaTabla, 72, 82, 95);
                case "Formato", "Butaca", "Ticket" -> configurarColumna(columnaTabla, 65, 78, 92);
                case "Idioma" -> configurarColumna(columnaTabla, 90, 110, 130);
                case "Precio", "Subtotal" -> configurarColumna(columnaTabla, 82, 98, 115);
                case "Estado", "Tipo", "Tamaño" -> configurarColumna(columnaTabla, 95, 125, 165);
                case "Portada" -> configurarColumna(columnaTabla, 75, 90, 115);
                case "Título", "Película", "Nombre", "Producto" ->
                        configurarColumnaFlexible(columnaTabla, 120, 190);
                case "Sala", "Categoría" -> configurarColumnaFlexible(columnaTabla, 95, 135);
                case "Descripción", "Detalle" -> configurarColumnaFlexible(columnaTabla, 180, 300);
                default -> configurarColumnaFlexible(columnaTabla, 80, 120);
            }
        }
    }

    public static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void limpiar(DefaultTableModel model) {
        model.setRowCount(0);
    }
}
