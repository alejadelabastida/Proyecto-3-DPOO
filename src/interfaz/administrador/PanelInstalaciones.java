package interfaz.administrador;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Club;
import modelo.Deporte;
import modelo.Instalacion;

public class PanelInstalaciones extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String TODAS = "Todas";

    private final DefaultTableModel modelo;
    private final JComboBox<Object> filtroDisciplina;
    private final JCheckBox revisarDisponibilidad;
    private final CampoFecha fecha;
    private final CampoHora hora;
    private final JSpinner duracion;

    public PanelInstalaciones(Club club, Administrador administrador) {
        super(club, administrador, "Instalaciones",
                "Consulte las instalaciones del club y revise si estan libres en una fecha y hora.");
        modelo = Estilo.modeloTabla("Codigo", "Instalacion", "Disciplina", "Tipo", "Modalidades", "Reservas",
                "Sesiones", "Disponibilidad");
        filtroDisciplina = new JComboBox<>();
        filtroDisciplina.addItem(TODAS);
        for (Deporte d : Deporte.values()) {
            filtroDisciplina.addItem(d);
        }
        revisarDisponibilidad = new JCheckBox("Revisar disponibilidad");
        revisarDisponibilidad.setOpaque(false);
        fecha = new CampoFecha(LocalDate.now());
        hora = new CampoHora(LocalTime.of(10, 0));
        duracion = new JSpinner(new SpinnerNumberModel(60, 30, 240, 30));
        filtroDisciplina.addActionListener(e -> actualizar());
        revisarDisponibilidad.addActionListener(e -> actualizar());
        fecha.addChangeListener(e -> actualizar());
        hora.addChangeListener(e -> actualizar());
        duracion.addChangeListener(e -> actualizar());

        JPanel filtros = Estilo.barraBotones(new JLabel("Disciplina:"), filtroDisciplina, revisarDisponibilidad,
                new JLabel("Fecha:"), fecha, new JLabel("Hora:"), hora, new JLabel("Minutos:"), duracion);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 6));
        cuerpo.setOpaque(false);
        cuerpo.add(filtros, BorderLayout.NORTH);
        cuerpo.add(Estilo.scroll(Estilo.tabla(modelo)), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        Object filtro = filtroDisciplina.getSelectedItem();
        boolean revisar = revisarDisponibilidad.isSelected();
        fecha.setEnabled(revisar);
        hora.setEnabled(revisar);
        duracion.setEnabled(revisar);
        for (Instalacion i : club.getInstalaciones()) {
            if (filtro instanceof Deporte && i.getDisciplina() != filtro) {
                continue;
            }
            String disponibilidad = "-";
            if (revisar) {
                disponibilidad = i.disponible(fecha.getFecha(), hora.getHora(), (Integer) duracion.getValue())
                        ? "Disponible" : "Ocupada";
            }
            modelo.addRow(new Object[] { i.getCodigo(), i.getNombre(), i.getDisciplina(),
                    i.isTechada() ? "Techada" : "Al aire libre", i.getModalidadesPermitidas(), i.getReservas().size(),
                    i.getSesiones().size(), disponibilidad });
        }
    }
}
