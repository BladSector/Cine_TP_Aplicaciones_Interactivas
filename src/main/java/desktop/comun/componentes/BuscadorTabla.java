package desktop.comun.componentes;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import static desktop.comun.util.TextoBusqueda.normalizarBusqueda;

public class BuscadorTabla extends JPanel {
    public BuscadorTabla(JTable tabla) {
        super(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JTextField campo = new JTextField(24);
        campo.putClientProperty("JTextField.placeholderText", "Buscar...");
        add(new JLabel("Buscar:"));
        add(campo);
        conectarFiltro(tabla, campo);
    }

    public static void conectarFiltro(JTable tabla, JTextField campo) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) tabla.getModel());
        tabla.setRowSorter(sorter);
        configurarOrdenInicial(sorter, (DefaultTableModel) tabla.getModel());
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void actualizar() {
                String busqueda = normalizarBusqueda(campo.getText());
                sorter.setRowFilter(busqueda.isEmpty() ? null : new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int columna = 0; columna < entry.getValueCount(); columna++) {
                            if (normalizarBusqueda(String.valueOf(entry.getValue(columna))).contains(busqueda)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                actualizar();
            }
        });
    }

    private static void configurarOrdenInicial(TableRowSorter<DefaultTableModel> sorter,
                                                DefaultTableModel model) {
        int fechaHora = model.findColumn("Fecha y hora");
        if (fechaHora >= 0) {
            sorter.setSortKeys(List.of(new RowSorter.SortKey(fechaHora, SortOrder.DESCENDING)));
            return;
        }
        int fecha = model.findColumn("Fecha");
        int horario = model.findColumn("Horario");
        if (fecha >= 0 && horario >= 0) {
            sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(fecha, SortOrder.ASCENDING),
                    new RowSorter.SortKey(horario, SortOrder.ASCENDING)
            ));
            return;
        }
        int id = model.findColumn("ID");
        if (id >= 0) {
            sorter.setSortKeys(List.of(new RowSorter.SortKey(id, SortOrder.ASCENDING)));
        }
    }

}
