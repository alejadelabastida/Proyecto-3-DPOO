package presentacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import modelo.Club;
import modelo.Deporte;
import modelo.EncuentroTorneo;
import modelo.EstadisticaPractica;
import modelo.InscripcionDisciplina;
import modelo.InscripcionTorneo;
import modelo.Partido;
import modelo.PartidoPractica;
import modelo.RegistroRanking;
import modelo.Socio;
import modelo.Torneo;
import modelo.Usuario;

public class PanelEstadisticas extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String TODAS = "Todas";
    private static final DateTimeFormatter ETIQUETA = DateTimeFormatter.ofPattern("dd/MM");

    private final Club club;
    private final Usuario usuario;

    private final JComboBox<Deporte> disciplinaElo = new JComboBox<>();
    private final JComboBox<Integer> semanas = new JComboBox<>(new Integer[] { 4, 6, 8, 12 });
    private final GraficaLineas lineas = new GraficaLineas();
    private final JTable tablaMejores = Estilo.tabla("#", "Socio", "ELO");

    private final JComboBox<Object> disciplinaPastel = new JComboBox<>();
    private final JComboBox<String> tipoCompetencia = new JComboBox<>(new String[] { "Todas", "Practicas", "Torneos" });
    private final GraficaPastel pastel = new GraficaPastel();

    public PanelEstadisticas(Club club, Usuario usuario) {
        this.club = club;
        this.usuario = usuario;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        add(Estilo.titulo("Mis graficas"), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Evolucion del ranking ELO", panelLineas());
        pestanas.addTab("Mis resultados", panelPastel());
        add(pestanas, BorderLayout.CENTER);

        semanas.setSelectedItem(8);
        recargarFiltros();
    }

    private JPanel panelLineas() {
        JPanel filtros = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        filtros.setBackground(Estilo.FONDO);
        filtros.add(new JLabel("Disciplina:"));
        filtros.add(disciplinaElo);
        filtros.add(new JLabel("Ultimas semanas:"));
        filtros.add(semanas);
        disciplinaElo.addActionListener(e -> dibujarLineas());
        semanas.addActionListener(e -> dibujarLineas());

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.add(new JLabel("  Mejores del ranking en la disciplina"), BorderLayout.NORTH);
        derecha.add(Estilo.conScroll(tablaMejores), BorderLayout.CENTER);
        derecha.setPreferredSize(new Dimension(260, 200));

        JSplitPane centro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lineas, derecha);
        centro.setResizeWeight(0.8);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(centro, BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelPastel() {
        JPanel filtros = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        filtros.setBackground(Estilo.FONDO);
        filtros.add(new JLabel("Disciplina:"));
        filtros.add(disciplinaPastel);
        filtros.add(new JLabel("Tipo de competencia:"));
        filtros.add(tipoCompetencia);
        disciplinaPastel.addActionListener(e -> dibujarPastel());
        tipoCompetencia.addActionListener(e -> dibujarPastel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filtros, BorderLayout.NORTH);
        panel.add(pastel, BorderLayout.CENTER);
        return panel;
    }

    public void recargarFiltros() {
        disciplinaElo.removeAllItems();
        disciplinaPastel.removeAllItems();
        disciplinaPastel.addItem(TODAS);
        if (usuario instanceof Socio) {
            for (InscripcionDisciplina i : ((Socio) usuario).getInscripciones()) {
                disciplinaElo.addItem(i.getDisciplina());
                disciplinaPastel.addItem(i.getDisciplina());
            }
        }
        dibujarLineas();
        dibujarPastel();
    }

    // ---------------- Grafica de lineas ----------------

    private InscripcionDisciplina inscripcionDe(Deporte deporte) {
        for (InscripcionDisciplina i : ((Socio) usuario).getInscripciones()) {
            if (i.getDisciplina() == deporte) {
                return i;
            }
        }
        return null;
    }

    private void dibujarLineas() {
        if (disciplinaElo.getSelectedItem() == null || semanas.getSelectedItem() == null) {
            lineas.setDatos("Inscribase en una academia para ver su ranking", "ELO",
                    new ArrayList<String>(), new ArrayList<Integer>());
            Estilo.llenar(tablaMejores, new ArrayList<Object[]>());
            return;
        }
        Deporte deporte = (Deporte) disciplinaElo.getSelectedItem();
        int numeroSemanas = (Integer) semanas.getSelectedItem();
        InscripcionDisciplina inscripcion = inscripcionDe(deporte);

        List<String> etiquetas = new ArrayList<>();
        List<Integer> valores = new ArrayList<>();
        LocalDate semanaActual = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int k = numeroSemanas - 1; k >= 0; k--) {
            LocalDate inicio = semanaActual.minusWeeks(k);
            etiquetas.add(inicio.format(ETIQUETA));
            valores.add(DatosGraficas.eloAlFinalDe(inscripcion, inicio.plusDays(6)));
        }
        lineas.setDatos("Evolucion del ELO en " + deporte + " (ultimas " + numeroSemanas + " semanas)",
                "ELO", etiquetas, valores);
        llenarMejores(deporte);
    }

    private void llenarMejores(Deporte deporte) {
        List<InscripcionDisciplina> todas = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                for (InscripcionDisciplina i : ((Socio) u).getInscripciones()) {
                    if (i.getDisciplina() == deporte) {
                        todas.add(i);
                    }
                }
            }
        }
        List<Object[]> filas = new ArrayList<>();
        for (int puesto = 1; puesto <= 5 && !todas.isEmpty(); puesto++) {
            InscripcionDisciplina mejor = todas.get(0);
            for (InscripcionDisciplina i : todas) {
                if (i.getPuntajeRanking() > mejor.getPuntajeRanking()) {
                    mejor = i;
                }
            }
            todas.remove(mejor);
            Socio s = mejor.getSocio();
            String nombre = s.getNombre() + " " + s.getApellido() + (s == usuario ? " (yo)" : "");
            filas.add(new Object[] { puesto, nombre, mejor.getPuntajeRanking() });
        }
        Estilo.llenar(tablaMejores, filas);
    }

    // ---------------- Grafica de pastel ----------------

    private void dibujarPastel() {
        int[] conteo = contarResultados();
        List<String> etiquetas = new ArrayList<>();
        etiquetas.add("Victorias");
        etiquetas.add("Derrotas");
        etiquetas.add("Empates");
        List<Integer> valores = new ArrayList<>();
        valores.add(conteo[0]);
        valores.add(conteo[1]);
        valores.add(conteo[2]);
        pastel.setDatos("Mis resultados (" + disciplinaPastel.getSelectedItem() + ", "
                + tipoCompetencia.getSelectedItem() + ")", etiquetas, valores);
    }

    private int[] contarResultados() {
        return DatosGraficas.contarResultados(club, usuario, disciplinaPastel.getSelectedItem(),
                (String) tipoCompetencia.getSelectedItem());
    }
}
