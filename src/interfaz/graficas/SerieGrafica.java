package interfaz.graficas;

import java.awt.Color;

public class SerieGrafica {

    private final String nombre;
    private final Color color;
    private final double[] valores;

    public SerieGrafica(String nombre, Color color, double[] valores) {
        if (nombre == null || color == null || valores == null) {
            throw new IllegalArgumentException("La serie necesita nombre, color y valores");
        }
        this.nombre = nombre;
        this.color = color;
        this.valores = valores.clone();
    }

    public String getNombre() {
        return nombre;
    }

    public Color getColor() {
        return color;
    }

    public int getCantidad() {
        return valores.length;
    }

    public double getValor(int indice) {
        return valores[indice];
    }

    public double getMaximo() {
        double maximo = 0;
        for (double v : valores) {
            maximo = Math.max(maximo, v);
        }
        return maximo;
    }
}
