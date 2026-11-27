package presentacion;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class GraficaLineas extends JPanel {

    private static final long serialVersionUID = 1L;

    private String titulo = "";
    private String ejeY = "";
    private List<String> etiquetas = new ArrayList<>();
    private List<Integer> valores = new ArrayList<>();

    public GraficaLineas() {
        setBackground(Color.WHITE);
    }

    public void setDatos(String titulo, String ejeY, List<String> etiquetas, List<Integer> valores) {
        this.titulo = titulo;
        this.ejeY = ejeY;
        this.etiquetas = new ArrayList<>(etiquetas);
        this.valores = new ArrayList<>(valores);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(Estilo.AZUL);
        g2.setFont(Estilo.NORMAL.deriveFont(java.awt.Font.BOLD, 15f));
        g2.drawString(titulo, 20, 24);
        g2.setFont(Estilo.NORMAL.deriveFont(12f));
        fm = g2.getFontMetrics();

        if (valores.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.drawString("No hay datos para mostrar", 40, getHeight() / 2);
            g2.dispose();
            return;
        }

        int izquierda = 60;
        int derecha = 30;
        int arriba = 45;
        int abajo = 50;
        int ancho = getWidth() - izquierda - derecha;
        int alto = getHeight() - arriba - abajo;

        int minimo = valores.get(0);
        int maximo = valores.get(0);
        for (int v : valores) {
            minimo = Math.min(minimo, v);
            maximo = Math.max(maximo, v);
        }
        minimo -= 10;
        maximo += 10;

        for (int i = 0; i <= 4; i++) {
            int y = arriba + alto - i * alto / 4;
            int valor = minimo + i * (maximo - minimo) / 4;
            g2.setColor(new Color(225, 225, 225));
            g2.drawLine(izquierda, y, izquierda + ancho, y);
            g2.setColor(Color.DARK_GRAY);
            String texto = String.valueOf(valor);
            g2.drawString(texto, izquierda - 8 - fm.stringWidth(texto), y + 4);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(ejeY, 8, arriba - 10);
        g2.drawLine(izquierda, arriba, izquierda, arriba + alto);
        g2.drawLine(izquierda, arriba + alto, izquierda + ancho, arriba + alto);

        int n = valores.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = (n == 1) ? izquierda + ancho / 2 : izquierda + i * ancho / (n - 1);
            ys[i] = arriba + alto - (valores.get(i) - minimo) * alto / (maximo - minimo);
        }
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(Estilo.AZUL);
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < n; i++) {
            g2.setColor(Estilo.NARANJA);
            g2.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
            g2.setColor(Color.DARK_GRAY);
            String texto = String.valueOf(valores.get(i));
            g2.drawString(texto, xs[i] - fm.stringWidth(texto) / 2, ys[i] - 10);
            String etiqueta = etiquetas.get(i);
            g2.drawString(etiqueta, xs[i] - fm.stringWidth(etiqueta) / 2, arriba + alto + 18);
        }
        g2.dispose();
    }
}
