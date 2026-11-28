package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import interfaz.graficas.GraficaBarras;
import interfaz.graficas.SerieGrafica;
import modelo.Administrador;
import modelo.Club;
import modelo.IngresoDiario;
import modelo.RubroIngreso;

public class PanelIngresos extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("EEE dd/MM",
            Locale.forLanguageTag("es-CO"));

    private final CampoFecha inicio;
    private final Map<RubroIngreso, JCheckBox> filtrosRubro;
    private final JRadioButton porDia;
    private final JRadioButton acumulado;
    private final GraficaBarras grafica;
    private final DefaultTableModel modeloResumen;
    private final JLabel totalRango;
    private List<IngresoDiario> ultimoResultado;

    public PanelIngresos(Club club, Administrador administrador) {
        super(club, administrador, "Ingresos netos por rubro",
                "Compara tienda, cafeteria y mensualidades en un rango de "
                        + Club.DIAS_REPORTE_INGRESOS + " dias. Los valores ya descuentan el IVA y el impuesto al consumo.");
        ultimoResultado = new ArrayList<>();
        inicio = new CampoFecha(LocalDate.now().minusDays(Club.DIAS_REPORTE_INGRESOS - 1));
        inicio.addChangeListener(e -> actualizar());
        filtrosRubro = new EnumMap<>(RubroIngreso.class);
        for (RubroIngreso rubro : RubroIngreso.values()) {
            JCheckBox casilla = new JCheckBox(rubro.getEtiqueta(), true);
            casilla.setOpaque(false);
            casilla.setForeground(colorDe(rubro).darker());
            casilla.addActionListener(e -> actualizar());
            filtrosRubro.put(rubro, casilla);
        }
        porDia = new JRadioButton("Por dia", true);
        acumulado = new JRadioButton("Total del rango");
        porDia.setOpaque(false);
        acumulado.setOpaque(false);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(porDia);
        grupo.add(acumulado);
        porDia.addActionListener(e -> actualizar());
        acumulado.addActionListener(e -> actualizar());

        grafica = new GraficaBarras();
        grafica.setFormatos(Estilo::dinero, Estilo::dineroCorto);
        grafica.setMensajeVacio("No hay ingresos registrados en este rango de fechas");
        grafica.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));

        modeloResumen = Estilo.modeloTabla("Dia", "Tienda", "Cafeteria", "Mensualidades", "Total neto");
        totalRango = new JLabel(" ");
        totalRango.setFont(Estilo.FUENTE_SUBTITULO);
        totalRango.setForeground(Estilo.AZUL);

        JPanel filtros = Estilo.barraBotones(new JLabel("Desde:"), inicio,
                Estilo.boton("Ultimos " + Club.DIAS_REPORTE_INGRESOS + " dias", e -> inicio.setFecha(
                        LocalDate.now().minusDays(Club.DIAS_REPORTE_INGRESOS - 1))),
                new JLabel("   Rubros:"), filtrosRubro.get(RubroIngreso.TIENDA),
                filtrosRubro.get(RubroIngreso.CAFETERIA), filtrosRubro.get(RubroIngreso.MENSUALIDADES),
                new JLabel("   Vista:"), porDia, acumulado);

        JScrollPane resumen = Estilo.scroll(Estilo.tabla(modeloResumen));
        resumen.setPreferredSize(new Dimension(100, 170));
        JPanel inferior = new JPanel(new BorderLayout(0, 4));
        inferior.setOpaque(false);
        inferior.add(totalRango, BorderLayout.NORTH);
        inferior.add(resumen, BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setOpaque(false);
        cuerpo.add(filtros, BorderLayout.NORTH);
        cuerpo.add(grafica, BorderLayout.CENTER);
        cuerpo.add(inferior, BorderLayout.SOUTH);
        add(cuerpo, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        ultimoResultado = club.consultarIngresosNetos(codigoAdministrador(), inicio.getFecha());
        Set<RubroIngreso> rubros = getRubrosSeleccionados();
        LocalDate primero = ultimoResultado.get(0).getFecha();
        LocalDate ultimo = ultimoResultado.get(ultimoResultado.size() - 1).getFecha();
        String titulo = "Ingresos netos del " + primero + " al " + ultimo;
        if (porDia.isSelected()) {
            grafica.setDatos(titulo, categoriasPorDia(ultimoResultado), construirSeriesPorDia(ultimoResultado, rubros));
        } else {
            List<String> categoria = new ArrayList<>();
            categoria.add("Total " + Club.DIAS_REPORTE_INGRESOS + " dias");
            grafica.setDatos(titulo, categoria, construirSeriesAcumuladas(ultimoResultado, rubros));
        }
        llenarResumen(rubros);
    }

    public static List<String> categoriasPorDia(List<IngresoDiario> dias) {
        List<String> categorias = new ArrayList<>();
        for (IngresoDiario dia : dias) {
            categorias.add(dia.getFecha().format(FORMATO_DIA));
        }
        return categorias;
    }

    public static List<SerieGrafica> construirSeriesPorDia(List<IngresoDiario> dias, Set<RubroIngreso> rubros) {
        List<SerieGrafica> series = new ArrayList<>();
        for (RubroIngreso rubro : RubroIngreso.values()) {
            if (!rubros.contains(rubro)) {
                continue;
            }
            double[] valores = new double[dias.size()];
            for (int i = 0; i < dias.size(); i++) {
                valores[i] = dias.get(i).getValor(rubro);
            }
            series.add(new SerieGrafica(rubro.getEtiqueta(), colorDe(rubro), valores));
        }
        return series;
    }

    public static List<SerieGrafica> construirSeriesAcumuladas(List<IngresoDiario> dias, Set<RubroIngreso> rubros) {
        List<SerieGrafica> series = new ArrayList<>();
        for (RubroIngreso rubro : RubroIngreso.values()) {
            if (!rubros.contains(rubro)) {
                continue;
            }
            double total = 0;
            for (IngresoDiario dia : dias) {
                total += dia.getValor(rubro);
            }
            series.add(new SerieGrafica(rubro.getEtiqueta(), colorDe(rubro), new double[] { total }));
        }
        return series;
    }

    public static Color colorDe(RubroIngreso rubro) {
        switch (rubro) {
            case TIENDA:
                return Estilo.COLOR_TIENDA;
            case CAFETERIA:
                return Estilo.COLOR_CAFETERIA;
            default:
                return Estilo.COLOR_MENSUALIDADES;
        }
    }

    public Set<RubroIngreso> getRubrosSeleccionados() {
        Set<RubroIngreso> rubros = EnumSet.noneOf(RubroIngreso.class);
        for (Map.Entry<RubroIngreso, JCheckBox> entrada : filtrosRubro.entrySet()) {
            if (entrada.getValue().isSelected()) {
                rubros.add(entrada.getKey());
            }
        }
        return rubros;
    }

    public GraficaBarras getGrafica() {
        return grafica;
    }

    public void setInicio(LocalDate fecha) {
        inicio.setFecha(fecha);
    }

    public void setRubroVisible(RubroIngreso rubro, boolean visible) {
        filtrosRubro.get(rubro).setSelected(visible);
        actualizar();
    }

    public void setVistaAcumulada(boolean vistaAcumulada) {
        acumulado.setSelected(vistaAcumulada);
        porDia.setSelected(!vistaAcumulada);
        actualizar();
    }

    private void llenarResumen(Set<RubroIngreso> rubros) {
        modeloResumen.setRowCount(0);
        double total = 0;
        for (IngresoDiario dia : ultimoResultado) {
            double totalDia = 0;
            Object[] fila = new Object[5];
            fila[0] = dia.getFecha().format(FORMATO_DIA);
            RubroIngreso[] orden = RubroIngreso.values();
            for (int i = 0; i < orden.length; i++) {
                if (rubros.contains(orden[i])) {
                    fila[i + 1] = Estilo.dinero(dia.getValor(orden[i]));
                    totalDia += dia.getValor(orden[i]);
                } else {
                    fila[i + 1] = "-";
                }
            }
            fila[4] = Estilo.dinero(totalDia);
            total += totalDia;
            modeloResumen.addRow(fila);
        }
        totalRango.setText("Total neto de los rubros seleccionados: " + Estilo.dinero(total));
    }
}
