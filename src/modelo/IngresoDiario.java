package modelo;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

public class IngresoDiario {

    private final LocalDate fecha;
    private final Map<RubroIngreso, Double> valores;

    public IngresoDiario(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        this.fecha = fecha;
        this.valores = new EnumMap<>(RubroIngreso.class);
        for (RubroIngreso rubro : RubroIngreso.values()) {
            valores.put(rubro, 0.0);
        }
    }

    public void sumar(RubroIngreso rubro, double valor) {
        valores.put(rubro, redondear(valores.get(rubro) + valor));
    }

    public double getValor(RubroIngreso rubro) {
        return valores.get(rubro);
    }

    public double getTotal() {
        double total = 0;
        for (double valor : valores.values()) {
            total += valor;
        }
        return redondear(total);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    @Override
    public String toString() {
        return fecha + " | tienda: " + getValor(RubroIngreso.TIENDA) + " | cafeteria: "
                + getValor(RubroIngreso.CAFETERIA) + " | mensualidades: " + getValor(RubroIngreso.MENSUALIDADES)
                + " | total: " + getTotal();
    }
}
