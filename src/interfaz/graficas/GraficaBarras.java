package interfaz.graficas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

import javax.swing.JComponent;

public class GraficaBarras extends JComponent {

    private static final long serialVersionUID = 1L;
    private static final int MARGEN_IZQUIERDO = 96;
    private static final int MARGEN_DERECHO = 24;
    private static final int MARGEN_SUPERIOR = 64;
    private static final int MARGEN_INFERIOR = 44;
    private static final int LINEAS_GUIA = 5;
    private static final Color COLOR_EJES = new Color(0x9A, 0xA3, 0xB2);
    private static final Color COLOR_GUIAS = new Color(0xE3, 0xE7, 0xED);
    private static final Color COLOR_TEXTO = new Color(0x33, 0x3B, 0x48);

    private String titulo;
    private String mensajeVacio;
    private List<String> categorias;
    private List<SerieGrafica> series;
    private DoubleFunction<String> formatoValores;
    private DoubleFunction<String> formatoEje;
    private final List<Rectangle2D> zonas;
    private final List<String> descripciones;

    public GraficaBarras() {
        this.titulo = "";
        this.mensajeVacio = "No hay datos para mostrar";
        this.categorias = new ArrayList<>();
        this.series = new ArrayList<>();
        this.formatoValores = v -> String.format("%,.0f", v);
        this.formatoEje = formatoValores;
        this.zonas = new ArrayList<>();
        this.descripciones = new ArrayList<>();
        setPreferredSize(new Dimension(760, 420));
        setOpaque(true);
        setBackground(Color.WHITE);
        setToolTipText("");
    }

    public void setDatos(String titulo, List<String> categorias, List<SerieGrafica> series) {
        for (SerieGrafica serie : series) {
            if (serie.getCantidad() != categorias.size()) {
                throw new IllegalArgumentException("Cada serie debe tener un valor por categoria");
            }
        }
        this.titulo = titulo;
        this.categorias = new ArrayList<>(categorias);
        this.series = new ArrayList<>(series);
        repaint();
    }

    public void setFormatos(DoubleFunction<String> formatoValores, DoubleFunction<String> formatoEje) {
        this.formatoValores = formatoValores;
        this.formatoEje = formatoEje;
        repaint();
    }

    public void setMensajeVacio(String mensajeVacio) {
        this.mensajeVacio = mensajeVacio;
        repaint();
    }

    public List<String> getCategorias() {
        return new ArrayList<>(categorias);
    }

    public List<SerieGrafica> getSeries() {
        return new ArrayList<>(series);
    }

    public boolean estaVacia() {
        return calcularMaximo() <= 0;
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        for (int i = 0; i < zonas.size(); i++) {
            if (zonas.get(i).contains(evento.getPoint())) {
                return descripciones.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        zonas.clear();
        descripciones.clear();

        dibujarTitulo(g2);
        dibujarLeyenda(g2);

        int ancho = getWidth() - MARGEN_IZQUIERDO - MARGEN_DERECHO;
        int alto = getHeight() - MARGEN_SUPERIOR - MARGEN_INFERIOR;
        if (ancho <= 0 || alto <= 0) {
            g2.dispose();
            return;
        }
        double maximo = calcularMaximo();
        if (categorias.isEmpty() || series.isEmpty() || maximo <= 0) {
            dibujarEjes(g2, ancho, alto, 1, false);
            dibujarMensajeVacio(g2, ancho, alto);
            g2.dispose();
            return;
        }
        double tope = redondearTope(maximo);
        dibujarEjes(g2, ancho, alto, tope, true);
        dibujarBarras(g2, ancho, alto, tope);
        g2.dispose();
    }

    private void dibujarTitulo(Graphics2D g2) {
        g2.setColor(COLOR_TEXTO);
        g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
        g2.drawString(titulo, MARGEN_IZQUIERDO, 24);
    }

    private void dibujarLeyenda(Graphics2D g2) {
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int x = MARGEN_IZQUIERDO;
        int y = 44;
        for (SerieGrafica serie : series) {
            g2.setColor(serie.getColor());
            g2.fill(new RoundRectangle2D.Double(x, y - 10, 12, 12, 4, 4));
            g2.setColor(COLOR_TEXTO);
            g2.drawString(serie.getNombre(), x + 17, y);
            x += 17 + fm.stringWidth(serie.getNombre()) + 22;
        }
    }

    private void dibujarEjes(Graphics2D g2, int ancho, int alto, double tope, boolean conValores) {
        g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
        FontMetrics fm = g2.getFontMetrics();
        int base = MARGEN_SUPERIOR + alto;
        for (int i = 0; i <= LINEAS_GUIA; i++) {
            int y = base - (int) Math.round(alto * i / (double) LINEAS_GUIA);
            g2.setColor(i == 0 ? COLOR_EJES : COLOR_GUIAS);
            g2.setStroke(new BasicStroke(i == 0 ? 1.4f : 1f));
            g2.drawLine(MARGEN_IZQUIERDO, y, MARGEN_IZQUIERDO + ancho, y);
            if (conValores) {
                String etiqueta = formatoEje.apply(tope * i / LINEAS_GUIA);
                g2.setColor(COLOR_TEXTO);
                g2.drawString(etiqueta, MARGEN_IZQUIERDO - 8 - fm.stringWidth(etiqueta), y + fm.getAscent() / 2 - 1);
            }
        }
        if (categorias.isEmpty()) {
            return;
        }
        double anchoGrupo = ancho / (double) categorias.size();
        g2.setColor(COLOR_TEXTO);
        for (int i = 0; i < categorias.size(); i++) {
            String texto = categorias.get(i);
            int x = (int) (MARGEN_IZQUIERDO + anchoGrupo * i + anchoGrupo / 2 - fm.stringWidth(texto) / 2.0);
            g2.drawString(texto, x, base + 20);
        }
    }

    private void dibujarBarras(Graphics2D g2, int ancho, int alto, double tope) {
        int base = MARGEN_SUPERIOR + alto;
        double anchoGrupo = ancho / (double) categorias.size();
        double anchoUtil = anchoGrupo * 0.78;
        double anchoBarra = anchoUtil / series.size();
        g2.setFont(getFont().deriveFont(Font.PLAIN, 10f));
        FontMetrics fm = g2.getFontMetrics();
        for (int c = 0; c < categorias.size(); c++) {
            double inicioGrupo = MARGEN_IZQUIERDO + anchoGrupo * c + (anchoGrupo - anchoUtil) / 2;
            for (int s = 0; s < series.size(); s++) {
                SerieGrafica serie = series.get(s);
                double valor = serie.getValor(c);
                double altoBarra = alto * (valor / tope);
                double x = inicioGrupo + anchoBarra * s + 2;
                double y = base - altoBarra;
                double anchoReal = Math.max(anchoBarra - 4, 2);
                Rectangle2D barra = new Rectangle2D.Double(x, y, anchoReal, altoBarra);
                g2.setColor(serie.getColor());
                g2.fill(new RoundRectangle2D.Double(x, y, anchoReal, Math.max(altoBarra, 0), 6, 6));
                if (altoBarra > 3) {
                    g2.fill(new Rectangle2D.Double(x, base - Math.min(3, altoBarra), anchoReal, Math.min(3, altoBarra)));
                }
                zonas.add(new Rectangle2D.Double(x, Math.min(y, base - 8), anchoReal, Math.max(altoBarra, 8)));
                descripciones.add(categorias.get(c) + " - " + serie.getNombre() + ": " + formatoValores.apply(valor));
                if (valor > 0) {
                    String etiqueta = formatoEje.apply(valor);
                    int anchoTexto = fm.stringWidth(etiqueta);
                    if (anchoTexto <= anchoBarra + 6) {
                        g2.setColor(COLOR_TEXTO);
                        g2.drawString(etiqueta, (float) (x + anchoReal / 2 - anchoTexto / 2.0), (float) (y - 4));
                    }
                }
            }
        }
    }

    private void dibujarMensajeVacio(Graphics2D g2, int ancho, int alto) {
        g2.setFont(getFont().deriveFont(Font.ITALIC, 13f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(COLOR_EJES);
        int x = MARGEN_IZQUIERDO + ancho / 2 - fm.stringWidth(mensajeVacio) / 2;
        g2.drawString(mensajeVacio, x, MARGEN_SUPERIOR + alto / 2);
    }

    private double calcularMaximo() {
        double maximo = 0;
        for (SerieGrafica serie : series) {
            maximo = Math.max(maximo, serie.getMaximo());
        }
        return maximo;
    }

    private static double redondearTope(double maximo) {
        double magnitud = Math.pow(10, Math.floor(Math.log10(maximo)));
        double[] pasos = { 1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10 };
        for (double paso : pasos) {
            if (paso * magnitud >= maximo * 1.05) {
                return paso * magnitud;
            }
        }
        return 10 * magnitud;
    }
}
