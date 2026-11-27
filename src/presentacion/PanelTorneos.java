package presentacion;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import modelo.Club;
import modelo.EncuentroTorneo;
import modelo.InscripcionTorneo;
import modelo.ReglaNegocioException;
import modelo.Torneo;
import modelo.Usuario;

/** Pestana de torneos: consultar, inscribirse, inscribir pareja, retirarse, ver clasificacion y resultados. */
public class PanelTorneos extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Club club;
    private final Usuario usuario;
    private final JTable tablaTorneos = Estilo.tabla("Torneo", "Deporte", "Tipo", "Modalidad", "Estado", "Fecha", "Cupos libres", "Mi inscripcion");
    private final JTable tablaInscritos = Estilo.tabla("Inscritos", "Cupo", "Tarifa");
    private final JLabel resumen = new JLabel(" ");
    private List<Torneo> torneos = new ArrayList<>();

    public PanelTorneos(Club club, Usuario usuario) {
        this.club = club;
        this.usuario = usuario;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);

        add(Estilo.titulo("Torneos"), BorderLayout.NORTH);

        JSplitPane centro = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                Estilo.conScroll(tablaTorneos), Estilo.conScroll(tablaInscritos));
        centro.setResizeWeight(0.65);
        add(centro, BorderLayout.CENTER);

        JButton inscribirme = Estilo.boton("Inscribirme");
        JButton pareja = Estilo.boton("Inscribir pareja");
        JButton retirarme = Estilo.boton("Retirarme");
        JButton clasificacion = Estilo.boton("Clasificacion y resultados");
        JButton actualizar = Estilo.boton("Actualizar");
        inscribirme.addActionListener(e -> inscribirme());
        pareja.addActionListener(e -> inscribirPareja());
        retirarme.addActionListener(e -> retirarme());
        clasificacion.addActionListener(e -> verClasificacion());
        actualizar.addActionListener(e -> actualizar());

        JPanel sur = new JPanel(new BorderLayout());
        sur.setBackground(Estilo.FONDO);
        sur.add(Estilo.barraDeBotones(inscribirme, pareja, retirarme, clasificacion, actualizar), BorderLayout.NORTH);
        resumen.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 8, 10));
        sur.add(resumen, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        tablaTorneos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarInscritos();
            }
        });
        actualizar();
    }

    public void actualizar() {
        torneos = club.getTorneos();
        int elegida = tablaTorneos.getSelectedRow();
        if (elegida < 0) {
            elegida = 0;
        }
        List<Object[]> filas = new ArrayList<>();
        for (Torneo t : torneos) {
            InscripcionTorneo mia = t.buscarInscripcionDe(usuario);
            filas.add(new Object[] { t.getNombre(), t.getDeporte(), t.getTipo(),
                    t.esDeConjunto() ? "Equipos (" + t.getCategoria() + ")" : t.getModalidad() + " (" + t.getNivel() + ")",
                    t.getEstado(), t.getFecha(), t.cuposLibres() + " de " + t.getCupos(),
                    mia == null ? "-" : "Inscrito (" + mia.getTipoCupo() + ")" });
        }
        Estilo.llenar(tablaTorneos, filas);
        if (elegida < torneos.size()) {
            tablaTorneos.setRowSelectionInterval(elegida, elegida);
        }
        mostrarInscritos();
    }

    private Torneo torneoElegido() {
        int fila = tablaTorneos.getSelectedRow();
        if (fila < 0 || fila >= torneos.size()) {
            throw new ReglaNegocioException("Seleccione un torneo de la tabla");
        }
        return torneos.get(fila);
    }

    private void mostrarInscritos() {
        List<Object[]> filas = new ArrayList<>();
        int fila = tablaTorneos.getSelectedRow();
        if (fila >= 0 && fila < torneos.size()) {
            Torneo t = torneos.get(fila);
            for (InscripcionTorneo i : t.getInscripciones()) {
                filas.add(new Object[] { i.getNombreParticipantes(), i.getTipoCupo(), i.getTarifaCobrada() });
            }
            resumen.setText("Cupos preferenciales: " + t.cuposPreferenciales()
                    + " | tarifa por persona: " + t.getTarifa());
        } else {
            resumen.setText("Seleccione un torneo para ver sus inscritos.");
        }
        Estilo.llenar(tablaInscritos, filas);
    }

    private void inscribirme() {
        try {
            Torneo torneo = torneoElegido();
            if (torneo.esDeConjunto()) {
                throw new ReglaNegocioException("Es un torneo de equipos: lo inscribe el entrenador responsable de su equipo");
            }
            InscripcionTorneo i = club.inscribirJugadorEnTorneo(usuario.getCodigo(), torneo);
            Estilo.aviso(this, "Inscrito en " + torneo.getNombre() + " con cupo " + i.getTipoCupo()
                    + ". Tarifa: " + i.getTarifaCobrada());
            actualizar();
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private void inscribirPareja() {
        try {
            Torneo torneo = torneoElegido();
            String login = JOptionPane.showInputDialog(this, "Usuario de su pareja:");
            if (login == null) {
                return;
            }
            int codigo = club.buscarCodigoPorLogin(login.trim());
            InscripcionTorneo i = club.inscribirParejaEnTorneo(usuario.getCodigo(), codigo, torneo);
            Estilo.aviso(this, "Pareja inscrita con cupo " + i.getTipoCupo() + ". Tarifa total: " + i.getTarifaCobrada());
            actualizar();
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private void retirarme() {
        try {
            Torneo torneo = torneoElegido();
            if (Estilo.confirmar(this, "Seguro que desea retirarse de " + torneo.getNombre() + "?")) {
                club.retirarDeTorneo(usuario.getCodigo(), torneo);
                Estilo.aviso(this, "Se retiro del torneo y el cupo quedo libre.");
                actualizar();
            }
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private void verClasificacion() {
        try {
            Torneo torneo = torneoElegido();
            JTable posiciones = Estilo.tabla("#", "Participante", "Puntos", "Diferencia");
            List<Object[]> filas = new ArrayList<>();
            int puesto = 1;
            for (InscripcionTorneo i : torneo.getPosiciones()) {
                filas.add(new Object[] { puesto++, i.getNombreParticipantes(), torneo.puntosDe(i), torneo.diferenciaDe(i) });
            }
            Estilo.llenar(posiciones, filas);

            JTable resultados = Estilo.tabla("Local", "Marcador", "Visitante");
            List<Object[]> encuentros = new ArrayList<>();
            for (EncuentroTorneo e : torneo.getEncuentros()) {
                String marcador = e.isJugado() ? e.getMarcadorLocal() + " - " + e.getMarcadorVisitante() : "pendiente";
                encuentros.add(new Object[] { e.getLocal().getNombreParticipantes(), marcador,
                        e.getVisitante().getNombreParticipantes() });
            }
            Estilo.llenar(resultados, encuentros);

            JSplitPane contenido = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    Estilo.conScroll(posiciones), Estilo.conScroll(resultados));
            contenido.setResizeWeight(0.5);
            JDialog dialogo = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this),
                    "Clasificacion: " + torneo.getNombre());
            dialogo.add(contenido);
            dialogo.setSize(new Dimension(620, 420));
            dialogo.setLocationRelativeTo(this);
            dialogo.setVisible(true);
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }
}
