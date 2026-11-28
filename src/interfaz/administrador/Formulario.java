package interfaz.administrador;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import modelo.ReglaNegocioException;

public class Formulario {

    private final JPanel panel;
    private int fila;

    public Formulario() {
        panel = new JPanel(new GridBagLayout());
        fila = 0;
    }

    public Formulario agregar(String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(5, 4, 5, 10);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        JLabel texto = new JLabel(etiqueta);
        texto.setFont(Estilo.FUENTE_NORMAL);
        panel.add(texto, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo, c);
        fila++;
        return this;
    }

    public Formulario agregarCompleto(JComponent componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(5, 4, 5, 4);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        panel.add(componente, c);
        fila++;
        return this;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean mostrar(Component padre, String titulo, Runnable alAceptar) {
        while (true) {
            int respuesta = JOptionPane.showConfirmDialog(padre, panel, titulo, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (respuesta != JOptionPane.OK_OPTION) {
                return false;
            }
            try {
                alAceptar.run();
                return true;
            } catch (ReglaNegocioException | IllegalArgumentException e) {
                Estilo.error(padre, e.getMessage());
            }
        }
    }
}
