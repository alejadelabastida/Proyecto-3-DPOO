package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.ASCUN;
import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Competencia;
import modelo.Deporte;
import modelo.Equipo;
import modelo.InscripcionEquipo;
import modelo.Jugador;
import modelo.ReglaNegocioException;
import modelo.TorneoDeLosCerros;

public class PanelCompetencias extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String LIGA = "Liga (Torneo de los Cerros)";
    private static final String GRUPOS = "Grupos y eliminacion (ASCUN)";

    private final DefaultTableModel modeloCompetencias;
    private final JTable tablaCompetencias;
    private final DefaultTableModel modeloTabla;
    private final List<Competencia> visibles;

    public PanelCompetencias(Club club, Administrador administrador) {
        super(club, administrador, "Competencias oficiales",
                "Registre ligas o torneos ASCUN, inscriba equipos con su nomina y consulte la tabla de posiciones.");
        visibles = new ArrayList<>();
        modeloCompetencias = Estilo.modeloTabla("Competencia", "Entidad", "Temporada", "Deporte", "Categoria",
                "Formato", "Equipos");
        tablaCompetencias = Estilo.tabla(modeloCompetencias);
        tablaCompetencias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarTabla();
            }
        });
        modeloTabla = Estilo.modeloTabla("Puesto", "Equipo", "Grupo", "Puntos", "Ganados", "Empatados", "Perdidos",
                "Nomina");

        JPanel superior = new JPanel(new BorderLayout(0, 6));
        superior.setOpaque(false);
        superior.add(Estilo.scroll(tablaCompetencias), BorderLayout.CENTER);
        superior.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Registrar competencia", e -> registrar()),
                Estilo.boton("Inscribir equipo", e -> inscribirEquipo())), BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new BorderLayout(0, 6));
        inferior.setOpaque(false);
        inferior.add(Estilo.subtitulo("Tabla de posiciones"), BorderLayout.NORTH);
        inferior.add(Estilo.scroll(Estilo.tabla(modeloTabla)), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, inferior);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        division.setOpaque(false);
        add(division, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        Competencia seleccionada = getSeleccionada();
        modeloCompetencias.setRowCount(0);
        visibles.clear();
        for (Competencia c : club.getCompetencias()) {
            visibles.add(c);
            modeloCompetencias.addRow(new Object[] { c.getNombre(), c.getEntidadOrganizadora(), c.getTemporada(),
                    c.getDeporte(), c.getCategoria(), (c instanceof ASCUN) ? "ASCUN" : "Liga",
                    c.getInscripciones().size() });
        }
        int indice = visibles.indexOf(seleccionada);
        if (indice >= 0) {
            tablaCompetencias.setRowSelectionInterval(indice, indice);
        }
        mostrarTabla();
    }

    private Competencia getSeleccionada() {
        int fila = tablaCompetencias.getSelectedRow();
        return (fila >= 0 && fila < visibles.size()) ? visibles.get(fila) : null;
    }

    private void mostrarTabla() {
        modeloTabla.setRowCount(0);
        Competencia competencia = getSeleccionada();
        if (competencia == null) {
            return;
        }
        List<InscripcionEquipo> tabla = club.consultarTablaPosiciones(competencia);
        for (int i = 0; i < tabla.size(); i++) {
            InscripcionEquipo ins = tabla.get(i);
            modeloTabla.addRow(new Object[] { i + 1, ins.getEquipo().getNombre(),
                    ins.getGrupo() == null ? "-" : ins.getGrupo(), ins.getPuntos(), ins.getGanados(),
                    ins.getEmpatados(), ins.getPerdidos(), ins.getNomina().size() });
        }
    }

    private void registrar() {
        JTextField nombre = new JTextField(22);
        JTextField entidad = new JTextField(22);
        JTextField temporada = new JTextField("2026-2", 10);
        JComboBox<Deporte> deporte = new JComboBox<>(
                new Deporte[] { Deporte.FUTBOL, Deporte.BALONCESTO, Deporte.VOLEIBOL });
        JComboBox<Categoria> categoria = new JComboBox<>(Categoria.values());
        JSpinner nomina = new JSpinner(new SpinnerNumberModel(7, 1, 40, 1));
        JComboBox<String> formato = new JComboBox<>(new String[] { LIGA, GRUPOS });
        new Formulario()
                .agregar("Nombre", nombre)
                .agregar("Entidad organizadora", entidad)
                .agregar("Temporada", temporada)
                .agregar("Deporte", deporte)
                .agregar("Categoria", categoria)
                .agregar("Nomina minima", nomina)
                .agregar("Formato", formato)
                .mostrar(this, "Registrar competencia", () -> {
                    Competencia nueva;
                    if (LIGA.equals(formato.getSelectedItem())) {
                        nueva = new TorneoDeLosCerros(nombre.getText().trim(), entidad.getText().trim(),
                                temporada.getText().trim(), (Deporte) deporte.getSelectedItem(),
                                (Categoria) categoria.getSelectedItem(), (Integer) nomina.getValue());
                    } else {
                        nueva = new ASCUN(nombre.getText().trim(), entidad.getText().trim(),
                                temporada.getText().trim(), (Deporte) deporte.getSelectedItem(),
                                (Categoria) categoria.getSelectedItem(), (Integer) nomina.getValue());
                    }
                    club.agregarCompetencia(nueva);
                    actualizar();
                });
    }

    private void inscribirEquipo() {
        ejecutar(() -> {
            Competencia competencia = getSeleccionada();
            if (competencia == null) {
                throw new ReglaNegocioException("Seleccione primero una competencia");
            }
            List<Equipo> equipos = club.getEquipos();
            if (equipos.isEmpty()) {
                throw new ReglaNegocioException("No hay equipos registrados");
            }
            JComboBox<String> opciones = new JComboBox<>();
            for (Equipo e : equipos) {
                opciones.addItem(e.getNombre() + " | " + e.getDeporte() + " | " + e.getCategoria());
            }
            DefaultListModel<String> modeloNomina = new DefaultListModel<>();
            List<Jugador> disponibles = new ArrayList<>();
            JList<String> nomina = new JList<>(modeloNomina);
            nomina.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            nomina.setVisibleRowCount(8);
            Runnable cargarJugadores = () -> {
                modeloNomina.clear();
                disponibles.clear();
                for (Jugador j : equipos.get(opciones.getSelectedIndex()).getJugadores()) {
                    if (!j.estaSuspendido()) {
                        disponibles.add(j);
                        modeloNomina.addElement("#" + j.getNumeroCamiseta() + " " + j.getNombre() + " " + j.getApellido());
                    }
                }
                if (modeloNomina.isEmpty()) {
                    nomina.clearSelection();
                } else {
                    nomina.setSelectionInterval(0, modeloNomina.size() - 1);
                }
            };
            opciones.addActionListener(e -> cargarJugadores.run());
            cargarJugadores.run();
            JScrollPane scroll = Estilo.scroll(nomina);
            scroll.setPreferredSize(new Dimension(320, 170));
            new Formulario().agregar("Equipo", opciones).agregar("Nomina (Ctrl + clic)", scroll)
                    .mostrar(this, "Inscribir equipo en " + competencia.getNombre(), () -> {
                        Equipo equipo = equipos.get(opciones.getSelectedIndex());
                        if (equipo.getEntrenadorResponsable() == null) {
                            throw new ReglaNegocioException("El equipo no tiene entrenador responsable, asignelo primero");
                        }
                        List<Jugador> elegidos = new ArrayList<>();
                        for (int indice : nomina.getSelectedIndices()) {
                            elegidos.add(disponibles.get(indice));
                        }
                        club.inscribirEquipo(equipo.getEntrenadorResponsable().getCodigo(), equipo, competencia, elegidos);
                    });
        });
    }
}
