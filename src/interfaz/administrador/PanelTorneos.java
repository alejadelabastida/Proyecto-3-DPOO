package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.EncuentroTorneo;
import modelo.EstadoTorneo;
import modelo.InscripcionTorneo;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.ReglaNegocioException;
import modelo.TipoTorneo;
import modelo.Torneo;

public class PanelTorneos extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String TODOS = "Todos";

    private final JComboBox<Object> filtroEstado;
    private final DefaultTableModel modeloTorneos;
    private final JTable tablaTorneos;
    private final DefaultTableModel modeloInscritos;
    private final DefaultTableModel modeloEncuentros;
    private final DefaultTableModel modeloPosiciones;
    private final JTextArea historial;
    private final JLabel resumenCupos;
    private final List<Torneo> torneosVisibles;

    public PanelTorneos(Club club, Administrador administrador) {
        super(club, administrador, "Torneos internos",
                "Cree torneos, inicielos, registre resultados, consulte la clasificacion y finalicelos.");
        torneosVisibles = new ArrayList<>();
        filtroEstado = new JComboBox<>();
        filtroEstado.addItem(TODOS);
        for (EstadoTorneo estado : EstadoTorneo.values()) {
            filtroEstado.addItem(estado);
        }
        filtroEstado.addActionListener(e -> actualizar());

        modeloTorneos = Estilo.modeloTabla("Nombre", "Deporte", "Categoria / nivel", "Tipo", "Estado", "Fecha",
                "Cupos", "Tarifa");
        tablaTorneos = Estilo.tabla(modeloTorneos);
        tablaTorneos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });

        modeloInscritos = Estilo.modeloTabla("Participante(s)", "Tipo de cupo", "Tarifa cobrada", "Fecha");
        modeloEncuentros = Estilo.modeloTabla("#", "Local", "Visitante", "Marcador", "Estado");
        modeloPosiciones = Estilo.modeloTabla("Puesto", "Participante(s)", "Puntos", "Diferencia");
        historial = new JTextArea();
        historial.setEditable(false);
        historial.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resumenCupos = new JLabel(" ");
        resumenCupos.setFont(Estilo.FUENTE_NORMAL);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Inscritos", Estilo.scroll(Estilo.tabla(modeloInscritos)));
        pestanas.addTab("Encuentros", Estilo.scroll(Estilo.tabla(modeloEncuentros)));
        pestanas.addTab("Clasificacion", Estilo.scroll(Estilo.tabla(modeloPosiciones)));
        pestanas.addTab("Historial", Estilo.scroll(historial));

        JPanel detalle = new JPanel(new BorderLayout(0, 6));
        detalle.setOpaque(false);
        detalle.add(resumenCupos, BorderLayout.NORTH);
        detalle.add(pestanas, BorderLayout.CENTER);

        JPanel superior = new JPanel(new BorderLayout(0, 6));
        superior.setOpaque(false);
        superior.add(Estilo.barraBotones(new JLabel("Estado:"), filtroEstado), BorderLayout.NORTH);
        superior.add(Estilo.scroll(tablaTorneos), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, detalle);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        division.setOpaque(false);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 8));
        cuerpo.setOpaque(false);
        cuerpo.add(division, BorderLayout.CENTER);
        cuerpo.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Crear torneo", e -> crearTorneo()),
                Estilo.boton("Iniciar torneo", e -> iniciarTorneo()),
                Estilo.boton("Registrar resultado", e -> registrarResultado()),
                Estilo.boton("Finalizar torneo", e -> finalizarTorneo())), BorderLayout.SOUTH);
        add(cuerpo, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        Torneo seleccionado = getTorneoSeleccionado();
        modeloTorneos.setRowCount(0);
        torneosVisibles.clear();
        Object filtro = filtroEstado.getSelectedItem();
        List<Torneo> fuente = (filtro instanceof EstadoTorneo)
                ? club.consultarTorneosPorEstado((EstadoTorneo) filtro)
                : club.getTorneos();
        for (Torneo t : fuente) {
            torneosVisibles.add(t);
            modeloTorneos.addRow(new Object[] { t.getNombre(), t.getDeporte(), describirCategoria(t), t.getTipo(),
                    t.getEstado(), t.getFecha(), t.cuposOcupados() + "/" + t.getCupos(), Estilo.dinero(t.getTarifa()) });
        }
        int indice = torneosVisibles.indexOf(seleccionado);
        if (indice >= 0) {
            tablaTorneos.setRowSelectionInterval(indice, indice);
        }
        mostrarDetalle();
    }

    public Torneo getTorneoSeleccionado() {
        int fila = tablaTorneos.getSelectedRow();
        return (fila >= 0 && fila < torneosVisibles.size()) ? torneosVisibles.get(fila) : null;
    }

    public void seleccionar(Torneo torneo) {
        int indice = torneosVisibles.indexOf(torneo);
        if (indice >= 0) {
            tablaTorneos.setRowSelectionInterval(indice, indice);
        }
    }

    private void mostrarDetalle() {
        modeloInscritos.setRowCount(0);
        modeloEncuentros.setRowCount(0);
        modeloPosiciones.setRowCount(0);
        historial.setText("");
        Torneo torneo = getTorneoSeleccionado();
        if (torneo == null) {
            resumenCupos.setText("Seleccione un torneo para ver su detalle.");
            return;
        }
        resumenCupos.setText(torneo.getNombre() + "  |  cupos libres: " + torneo.cuposLibres() + " de "
                + torneo.getCupos() + "  |  preferenciales: " + torneo.cuposPreferenciales() + "  |  recaudado: "
                + Estilo.dinero(torneo.totalRecaudado()));
        for (InscripcionTorneo i : torneo.getInscripciones()) {
            modeloInscritos.addRow(new Object[] { i.getNombreParticipantes(), i.getTipoCupo(),
                    Estilo.dinero(i.getTarifaCobrada()), i.getFecha() });
        }
        List<EncuentroTorneo> encuentros = torneo.getEncuentros();
        for (int i = 0; i < encuentros.size(); i++) {
            EncuentroTorneo e = encuentros.get(i);
            String marcador = e.isJugado() ? e.getMarcadorLocal() + " - " + e.getMarcadorVisitante() : "-";
            modeloEncuentros.addRow(new Object[] { i + 1, e.getLocal().getNombreParticipantes(),
                    e.getVisitante().getNombreParticipantes(), marcador, e.isJugado() ? "Jugado" : "Pendiente" });
        }
        List<InscripcionTorneo> posiciones = torneo.getPosiciones();
        for (int i = 0; i < posiciones.size(); i++) {
            InscripcionTorneo p = posiciones.get(i);
            modeloPosiciones.addRow(new Object[] { i + 1, p.getNombreParticipantes(), torneo.puntosDe(p),
                    torneo.diferenciaDe(p) });
        }
        historial.setText(String.join("\n", club.consultarHistorialTorneo(torneo)));
        historial.setCaretPosition(0);
    }

    private String describirCategoria(Torneo t) {
        return t.esDeConjunto() ? "Categoria " + t.getCategoria() : t.getNivel() + " / " + t.getModalidad();
    }

    private Torneo exigirSeleccion() {
        Torneo torneo = getTorneoSeleccionado();
        if (torneo == null) {
            throw new ReglaNegocioException("Seleccione primero un torneo de la tabla");
        }
        return torneo;
    }

    private void crearTorneo() {
        JTextField nombre = new JTextField(22);
        JComboBox<TipoTorneo> tipo = new JComboBox<>(TipoTorneo.values());
        JComboBox<Deporte> deporte = new JComboBox<>(Deporte.values());
        JComboBox<Categoria> categoria = new JComboBox<>(Categoria.values());
        JComboBox<Nivel> nivel = new JComboBox<>(Nivel.values());
        JComboBox<Modalidad> modalidad = new JComboBox<>(Modalidad.values());
        JSpinner cupos = new JSpinner(new SpinnerNumberModel(8, 1, 64, 1));
        JSpinner tarifa = new JSpinner(new SpinnerNumberModel(30000.0, 0.0, 10_000_000.0, 1000.0));
        CampoFecha fecha = new CampoFecha(LocalDate.now().plusDays(7));
        Runnable ajustar = () -> {
            boolean conjunto = esDeConjunto((Deporte) deporte.getSelectedItem());
            categoria.setEnabled(conjunto);
            nivel.setEnabled(!conjunto);
            modalidad.setEnabled(!conjunto);
            tarifa.setEnabled(tipo.getSelectedItem() == TipoTorneo.COMPETITIVO);
        };
        deporte.addActionListener(e -> ajustar.run());
        tipo.addActionListener(e -> ajustar.run());
        ajustar.run();

        Formulario formulario = new Formulario()
                .agregar("Nombre", nombre)
                .agregar("Tipo", tipo)
                .agregar("Deporte", deporte)
                .agregar("Categoria", categoria)
                .agregar("Nivel", nivel)
                .agregar("Modalidad", modalidad)
                .agregar("Cupos", cupos)
                .agregar("Tarifa de inscripcion", tarifa)
                .agregar("Fecha", fecha);
        formulario.mostrar(this, "Crear torneo", () -> {
            if (fecha.getFecha().isBefore(LocalDate.now())) {
                throw new ReglaNegocioException("La fecha del torneo no puede estar en el pasado");
            }
            boolean conjunto = esDeConjunto((Deporte) deporte.getSelectedItem());
            TipoTorneo tipoElegido = (TipoTorneo) tipo.getSelectedItem();
            Torneo nuevo = club.crearTorneo(codigoAdministrador(), nombre.getText().trim(), tipoElegido,
                    (Deporte) deporte.getSelectedItem(),
                    conjunto ? (Categoria) categoria.getSelectedItem() : null,
                    conjunto ? null : (Nivel) nivel.getSelectedItem(),
                    conjunto ? null : (Modalidad) modalidad.getSelectedItem(),
                    (Integer) cupos.getValue(),
                    tipoElegido == TipoTorneo.COMPETITIVO ? (Double) tarifa.getValue() : 0,
                    fecha.getFecha());
            filtroEstado.setSelectedItem(TODOS);
            actualizar();
            seleccionar(nuevo);
            Estilo.info(this, "Torneo creado. Cupos preferenciales: " + nuevo.cuposPreferenciales() + ".");
        });
    }

    private boolean esDeConjunto(Deporte deporte) {
        return deporte == Deporte.FUTBOL || deporte == Deporte.BALONCESTO || deporte == Deporte.VOLEIBOL;
    }

    private void iniciarTorneo() {
        ejecutar(() -> {
            Torneo torneo = exigirSeleccion();
            if (Estilo.confirmar(this, "Iniciar el torneo " + torneo.getNombre() + "?")) {
                club.iniciarTorneo(codigoAdministrador(), torneo);
                Estilo.info(this, "Torneo iniciado con " + torneo.getEncuentros().size() + " encuentros.");
            }
        });
    }

    private void registrarResultado() {
        ejecutar(() -> {
            Torneo torneo = exigirSeleccion();
            List<EncuentroTorneo> pendientes = new ArrayList<>();
            for (EncuentroTorneo e : torneo.getEncuentros()) {
                if (!e.isJugado()) {
                    pendientes.add(e);
                }
            }
            if (pendientes.isEmpty()) {
                throw new ReglaNegocioException("El torneo no tiene encuentros pendientes");
            }
            JComboBox<String> encuentro = new JComboBox<>();
            for (EncuentroTorneo e : pendientes) {
                encuentro.addItem(e.getLocal().getNombreParticipantes() + "  vs  " + e.getVisitante().getNombreParticipantes());
            }
            JSpinner local = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            JSpinner visitante = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
            Formulario formulario = new Formulario()
                    .agregar("Encuentro", encuentro)
                    .agregar("Marcador local", local)
                    .agregar("Marcador visitante", visitante);
            formulario.mostrar(this, "Registrar resultado", () -> club.registrarResultado(codigoAdministrador(), torneo,
                    pendientes.get(encuentro.getSelectedIndex()), (Integer) local.getValue(),
                    (Integer) visitante.getValue()));
        });
    }

    private void finalizarTorneo() {
        ejecutar(() -> {
            Torneo torneo = exigirSeleccion();
            if (Estilo.confirmar(this, "Finalizar el torneo " + torneo.getNombre() + " y entregar los premios?")) {
                club.finalizarTorneo(codigoAdministrador(), torneo);
                String premio = torneo.getTipo() == TipoTorneo.COMPETITIVO
                        ? "Se repartieron los premios en efectivo."
                        : "El ganador recibio un bono de descuento.";
                Estilo.info(this, "Torneo finalizado. " + premio);
            }
        });
    }
}
