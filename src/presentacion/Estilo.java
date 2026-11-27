package presentacion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Estilo {

    public static final Color AZUL = new Color(30, 70, 120);
    public static final Color AZUL_CLARO = new Color(225, 235, 248);
    public static final Color FONDO = Color.WHITE;
    public static final Color VERDE = new Color(46, 139, 87);
    public static final Color ROJO = new Color(190, 60, 60);
    public static final Color NARANJA = new Color(230, 150, 40);
    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(TITULO);
        etiqueta.setForeground(AZUL);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return etiqueta;
    }

    public static JButton boton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(NORMAL);
        boton.setBackground(AZUL);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        return boton;
    }

    public static JPanel barraDeBotones(JButton... botones) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        barra.setBackground(FONDO);
        for (JButton boton : botones) {
            barra.add(boton);
        }
        return barra;
    }

    public static JTable tabla(String... columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setFont(NORMAL);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        return tabla;
    }

    public static JScrollPane conScroll(JTable tabla) {
        return new JScrollPane(tabla);
    }

    public static DefaultTableModel modelo(JTable tabla) {
        return (DefaultTableModel) tabla.getModel();
    }

    public static void llenar(JTable tabla, List<Object[]> filas) {
        DefaultTableModel modelo = modelo(tabla);
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
    }

    public static void error(Component padre, Exception e) {
        JOptionPane.showMessageDialog(padre, e.getMessage(), "No se pudo", JOptionPane.WARNING_MESSAGE);
    }

    public static void aviso(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String pregunta) {
        return JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
