package presentacion;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class GraficaPastel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color[] COLORES = { Estilo.VERDE, Estilo.ROJO, Estilo.NARANJA, Estilo.AZUL };

    private String titulo = "";
    private List<String> etiquetas = new ArrayList<>();
    private List<Integer> valores = new ArrayList<>();

    public GraficaPastel() {
        setBackground(Color.WHITE);
    }

    public void setDatos(String titulo, List<String> etiquetas, List<Integer> valores) {
        this.titulo = titulo;
        this.etiquetas = new ArrayList<>(etiquetas);
        this.valores = new ArrayList<>(valores);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Estilo.AZUL);
        g2.setFont(Estilo.NORMAL.deriveFont(java.awt.Font.BOLD, 15f));
        g2.drawString(titulo, 20, 24);
        g2.setFont(Estilo.NORMAL.deriveFont(13f));
        FontMetrics fm = g2.getFontMetrics();

        int total = 0;
        for (int v : valores) {
            total += v;
        }
        if (total == 0) {
            g2.setColor(Color.GRAY);
            g2.drawString("No hay resultados para mostrar", 40, getHeight() / 2);
            g2.dispose();
            return;
        }

        int diametro = Math.min(getWidth() / 2, getHeight() - 70);
        int x = 30;
        int y = 45;
        double inicio = 90;
        for (int i = 0; i < valores.size(); i++) {
            double angulo = 360.0 * valores.get(i) / total;
            g2.setColor(COLORES[i % COLORES.length]);
            g2.fillArc(x, y, diametro, diametro, (int) Math.round(inicio), (int) Math.round(-angulo));
            inicio -= angulo;
        }

        int xLeyenda = x + diametro + 30;
        int yLeyenda = y + 30;
        for (int i = 0; i < valores.size(); i++) {
            g2.setColor(COLORES[i % COLORES.length]);
            g2.fillRect(xLeyenda, yLeyenda - 12, 16, 16);
            g2.setColor(Color.DARK_GRAY);
            int porcentaje = Math.round(100f * valores.get(i) / total);
            g2.drawString(etiquetas.get(i) + ": " + valores.get(i) + " (" + porcentaje + "%)",
                    xLeyenda + 24, yLeyenda);
            yLeyenda += 28;
        }
        g2.dispose();
    }
}
