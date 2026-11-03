package desktop.duenio.dialogos;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static desktop.comun.util.TextoBusqueda.normalizarBusqueda;

final class FormularioDialogo {
    private FormularioDialogo() {
    }

    static JPanel crear(String[] etiquetas, Component... componentes) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        for (int i = 0; i < etiquetas.length; i++) {
            if (componentes[i] instanceof JTextField campo && campo.getColumns() == 0) {
                campo.setColumns(22);
            }

            GridBagConstraints etiqueta = new GridBagConstraints();
            etiqueta.gridx = 0;
            etiqueta.gridy = i;
            etiqueta.anchor = GridBagConstraints.LINE_END;
            etiqueta.insets = new Insets(4, 4, 4, 8);
            panel.add(new JLabel(etiquetas[i]), etiqueta);

            GridBagConstraints control = new GridBagConstraints();
            control.gridx = 1;
            control.gridy = i;
            control.weightx = 1;
            control.fill = GridBagConstraints.HORIZONTAL;
            control.anchor = GridBagConstraints.LINE_START;
            control.insets = new Insets(4, 0, 4, 4);
            panel.add(componentes[i], control);
        }
        return panel;
    }

    static boolean confirmar(Component padre, String titulo, JPanel panel) {
        return JOptionPane.showConfirmDialog(padre, panel, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    static boolean contraseniasValidas(Component padre, String clave, String confirmacion) {
        if (!clave.equals(confirmacion)) {
            aviso(padre, "Las contraseñas no coinciden.");
            return false;
        }
        if (clave.length() < 4) {
            aviso(padre, "La contraseña debe tener al menos 4 caracteres.");
            return false;
        }
        return true;
    }

    static void seleccionarPorNombre(JComboBox<Opcion> combo, String nombre) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).nombre().equals(nombre)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    static void seleccionarPorId(JComboBox<Opcion> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    static void conectarBusqueda(JTextField campo, JComboBox<Opcion> combo, List<Opcion> opciones) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() {
                Opcion seleccionAnterior = combo.getSelectedItem() instanceof Opcion opcion ? opcion : null;
                String busqueda = normalizarBusqueda(campo.getText());

                combo.removeAllItems();
                opciones.stream()
                        .filter(opcion -> normalizarBusqueda(opcion.nombre()).contains(busqueda))
                        .forEach(combo::addItem);

                if (seleccionAnterior != null) {
                    seleccionarPorId(combo, seleccionAnterior.id());
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizar();
            }
        });
    }

}
