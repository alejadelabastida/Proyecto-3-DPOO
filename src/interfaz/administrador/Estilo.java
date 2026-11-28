package interfaz.administrador;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public final class Estilo {

    public static final Color AZUL = new Color(0x24, 0x36, 0x5E);
    public static final Color AZUL_MEDIO = new Color(0x3A, 0x55, 0x8F);
    public static final Color AZUL_CLARO = new Color(0xE6, 0xEC, 0xF6);
    public static final Color FONDO = new Color(0xF4, 0xF6, 0xF9);
    public static final Color BORDE = new Color(0xD5, 0xDB, 0xE5);
    public static final Color TEXTO_SUAVE = new Color(0x5F, 0x6B, 0x7D);
    public static final Color COLOR_TIENDA = new Color(0x2F, 0x6E, 0xD6);
    public static final Color COLOR_CAFETERIA = new Color(0xE0, 0x8A, 0x1F);
    public static final Color COLOR_MENSUALIDADES = new Color(0x2E, 0x9C, 0x6A);
    public static final Font FUENTE_TITULO = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    private static final DecimalFormat FORMATO_DINERO = new DecimalFormat("$ #,##0",
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));

    private Estilo() {
    }

    public static String dinero(double valor) {
        return FORMATO_DINERO.format(valor);
    }

    public static String dineroCorto(double valor) {
        if (valor >= 1_000_000) {
            return String.format(Locale.forLanguageTag("es-CO"), "$ %.1f M", valor / 1_000_000);
        }
        if (valor >= 1_000) {
            return String.format(Locale.forLanguageTag("es-CO"), "$ %.0f mil", valor / 1_000);
        }
        return dinero(valor);
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_TITULO);
        etiqueta.setForeground(AZUL);
        return etiqueta;
    }

    public static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_SUBTITULO);
        etiqueta.setForeground(AZUL);
        return etiqueta;
    }

    public static JButton boton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setFont(FUENTE_NORMAL);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.addActionListener(accion);
        return boton;
    }

    public static JButton botonPrincipal(String texto, ActionListener accion) {
        JButton boton = boton(texto, accion);
        boton.setBackground(AZUL);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
        return boton;
    }

    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(false);
        tabla.getTableHeader().setFont(FUENTE_NORMAL.deriveFont(Font.BOLD));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setGridColor(BORDE);
        return tabla;
    }

    public static JScrollPane scroll(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        return scroll;
    }

    public static JPanel barraBotones(Component... componentes) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        barra.setOpaque(false);
        for (Component c : componentes) {
            barra.add(c);
        }
        return barra;
    }

    public static JPanel tarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        return tarjeta;
    }

    public static Dimension ancho(int pixeles) {
        return new Dimension(pixeles, 28);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar la accion", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
