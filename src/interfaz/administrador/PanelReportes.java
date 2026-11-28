package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import modelo.Administrador;
import modelo.Club;
import modelo.Deporte;
import modelo.ReglaNegocioException;

public class PanelReportes extends PanelBase {

    private static final long serialVersionUID = 1L;

    public enum TipoReporte {
        VENTAS("Ventas por rubro (tienda, cafeteria y mensualidades)"),
        ASISTENCIA("Asistencia a entrenamientos"),
        RESULTADOS("Resultados de partidos"),
        RANKING("Evolucion del ranking por deporte"),
        RESERVAS("Historial de reservas"),
        PARTIDOS("Historial de partidos"),
        SESIONES("Historial de sesiones de entrenamiento");

        private final String etiqueta;

        TipoReporte(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final JComboBox<TipoReporte> tipo;
    private final JComboBox<Deporte> deporte;
    private final CampoFecha desde;
    private final CampoFecha hasta;
    private final JTextArea resultado;

    public PanelReportes(Club club, Administrador administrador) {
        super(club, administrador, "Consultas y reportes",
                "Genere reportes e historiales del club entre dos fechas.");
        tipo = new JComboBox<>(TipoReporte.values());
        deporte = new JComboBox<>(Deporte.values());
        desde = new CampoFecha(LocalDate.now().minusDays(30));
        hasta = new CampoFecha(LocalDate.now());
        resultado = new JTextArea();
        resultado.setEditable(false);
        resultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        tipo.addActionListener(e -> deporte.setEnabled(tipo.getSelectedItem() == TipoReporte.RANKING));
        deporte.setEnabled(false);

        JPanel filtros = Estilo.barraBotones(new JLabel("Reporte:"), tipo, new JLabel("Deporte:"), deporte,
                new JLabel("Desde:"), desde, new JLabel("Hasta:"), hasta,
                Estilo.botonPrincipal("Generar", e -> generar()));
        JPanel cuerpo = new JPanel(new BorderLayout(0, 6));
        cuerpo.setOpaque(false);
        cuerpo.add(filtros, BorderLayout.NORTH);
        cuerpo.add(Estilo.scroll(resultado), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
    }

    public void generar() {
        try {
            List<String> lineas = consultar((TipoReporte) tipo.getSelectedItem(), desde.getFecha(), hasta.getFecha());
            StringBuilder texto = new StringBuilder();
            texto.append(tipo.getSelectedItem()).append("  (").append(desde.getFecha()).append(" a ")
                    .append(hasta.getFecha()).append(")\n\n");
            for (String linea : lineas) {
                texto.append("  ").append(linea).append('\n');
            }
            resultado.setText(texto.toString());
            resultado.setCaretPosition(0);
        } catch (ReglaNegocioException e) {
            resultado.setText(e.getMessage());
        }
    }

    public List<String> consultar(TipoReporte reporte, LocalDate inicio, LocalDate fin) {
        switch (reporte) {
            case VENTAS:
                return club.reporteVentas(codigoAdministrador(), inicio, fin);
            case ASISTENCIA:
                return club.reporteAsistencia(inicio, fin);
            case RESULTADOS:
                return club.reporteResultados(inicio, fin);
            case RANKING:
                return club.reporteEvolucionRanking((Deporte) deporte.getSelectedItem(), inicio, fin);
            case RESERVAS:
                return club.historialReservas(inicio, fin);
            case PARTIDOS:
                return club.historialPartidos(inicio, fin);
            default:
                return club.historialSesiones(inicio, fin);
        }
    }

    public void configurar(TipoReporte reporte, LocalDate inicio, LocalDate fin) {
        tipo.setSelectedItem(reporte);
        desde.setFecha(inicio);
        hasta.setFecha(fin);
    }
}
