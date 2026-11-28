package interfaz.administrador;

import java.awt.BorderLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Club;
import modelo.Entrenador;
import modelo.EstadoSolicitud;
import modelo.ReglaNegocioException;
import modelo.SolicitudCambioTurno;
import modelo.Turno;

public class PanelTurnos extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DefaultTableModel modeloTurnos;
    private final DefaultTableModel modeloSolicitudes;
    private final JTable tablaSolicitudes;
    private final JCheckBox soloPendientes;
    private final List<SolicitudCambioTurno> solicitudesVisibles;

    public PanelTurnos(Club club, Administrador administrador) {
        super(club, administrador, "Turnos del personal",
                "Consulte los turnos semanales y apruebe o rechace los cambios. Minimo: 2 entrenadores y 1 fisioterapeuta.");
        solicitudesVisibles = new ArrayList<>();
        modeloTurnos = Estilo.modeloTabla("Empleado", "Rol", "Dia", "Inicio", "Fin");
        modeloSolicitudes = Estilo.modeloTabla("Empleado", "Turno actual", "Turno pedido", "Motivo", "Solicitada",
                "Estado", "Observacion");
        tablaSolicitudes = Estilo.tabla(modeloSolicitudes);
        soloPendientes = new JCheckBox("Solo pendientes", true);
        soloPendientes.setOpaque(false);
        soloPendientes.addActionListener(e -> actualizar());

        JPanel turnos = new JPanel(new BorderLayout(0, 6));
        turnos.setOpaque(false);
        turnos.add(Estilo.subtitulo("Turnos semanales"), BorderLayout.NORTH);
        turnos.add(Estilo.scroll(Estilo.tabla(modeloTurnos)), BorderLayout.CENTER);

        JPanel solicitudes = new JPanel(new BorderLayout(0, 6));
        solicitudes.setOpaque(false);
        solicitudes.add(Estilo.barraBotones(Estilo.subtitulo("Solicitudes de cambio de turno"), soloPendientes),
                BorderLayout.NORTH);
        solicitudes.add(Estilo.scroll(tablaSolicitudes), BorderLayout.CENTER);
        solicitudes.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Aprobar cambio", e -> aprobar()),
                Estilo.boton("Rechazar cambio", e -> rechazar())), BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, turnos, solicitudes);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        division.setOpaque(false);
        add(division, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        modeloTurnos.setRowCount(0);
        for (Turno t : club.consultarTurnos(codigoAdministrador())) {
            modeloTurnos.addRow(new Object[] { t.getEmpleado().getNombre() + " " + t.getEmpleado().getApellido(),
                    t.getEmpleado() instanceof Entrenador ? "Entrenador" : "Fisioterapeuta", t.getDiaSemana(),
                    t.getHoraInicio(), t.getHoraFin() });
        }
        modeloSolicitudes.setRowCount(0);
        solicitudesVisibles.clear();
        for (SolicitudCambioTurno s : administrador.consultarSolicitudesCambioTurno(club)) {
            if (soloPendientes.isSelected() && s.getEstado() != EstadoSolicitud.PENDIENTE) {
                continue;
            }
            solicitudesVisibles.add(s);
            Turno t = s.getTurno();
            String actual = t.getDiaSemana() + " " + t.getHoraInicio() + "-" + t.getHoraFin();
            String pedido = s.getNuevoDiaSemana() + " " + s.getNuevaHoraInicio() + "-" + s.getNuevaHoraFin();
            modeloSolicitudes.addRow(new Object[] { s.getEmpleado().getNombre(), actual, pedido, s.getMotivo(),
                    s.getFechaSolicitud().format(FORMATO_FECHA), s.getEstado(),
                    s.getObservacionEvaluacion() == null ? "" : s.getObservacionEvaluacion() });
        }
    }

    private SolicitudCambioTurno exigirSolicitud() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila < 0) {
            throw new ReglaNegocioException("Seleccione primero una solicitud");
        }
        return solicitudesVisibles.get(fila);
    }

    private void aprobar() {
        ejecutar(() -> {
            SolicitudCambioTurno solicitud = exigirSolicitud();
            club.aprobarCambio(solicitud);
            Estilo.info(this, "Cambio aprobado. Turno nuevo: " + solicitud.getTurno());
        });
    }

    private void rechazar() {
        ejecutar(() -> {
            SolicitudCambioTurno solicitud = exigirSolicitud();
            JTextField motivo = new JTextField(26);
            new Formulario().agregar("Motivo del rechazo", motivo).mostrar(this, "Rechazar cambio de turno", () -> {
                if (motivo.getText().trim().isEmpty()) {
                    throw new ReglaNegocioException("Escriba el motivo del rechazo");
                }
                club.rechazarCambio(solicitud, motivo.getText().trim());
            });
        });
    }
}
