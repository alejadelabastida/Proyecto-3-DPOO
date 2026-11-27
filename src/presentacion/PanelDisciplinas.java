package presentacion;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import modelo.Club;
import modelo.Convocatoria;
import modelo.Deporte;
import modelo.InscripcionDisciplina;
import modelo.Instalacion;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.Partido;
import modelo.PartidoPractica;
import modelo.ReglaNegocioException;
import modelo.Reserva;
import modelo.Socio;
import modelo.Usuario;

public class PanelDisciplinas extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Deporte[] INDIVIDUALES = { Deporte.TENIS, Deporte.PADEL, Deporte.TENIS_DE_MESA };

    private final Club club;
    private final Socio socio;
    private final JTable tablaAcademias = Estilo.tabla("Disciplina", "Nivel", "ELO", "Posicion");
    private final JTable tablaReservas = Estilo.tabla("Instalacion", "Fecha", "Inicio", "Fin", "Jugadores", "Modalidad");
    private final JTable tablaPracticas = Estilo.tabla("Disciplina", "Fecha", "Modalidad", "Jugadores", "Estado");
    private final JTable tablaConvocatorias = Estilo.tabla("Fecha", "Hora", "Instalacion", "Asistencia", "Nota", "Comentario");

    public PanelDisciplinas(Club club, Socio socio) {
        this.club = club;
        this.socio = socio;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        add(Estilo.titulo("Academias, reservas y entrenamientos"), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Mis academias", seccion(tablaAcademias, "Inscribirme en una academia", e -> inscribirEnAcademia()));
        pestanas.addTab("Reservas", seccion(tablaReservas, "Reservar una instalacion", e -> reservar()));
        pestanas.addTab("Practicas", seccion(tablaPracticas, "Programar una practica", e -> programarPractica()));
        pestanas.addTab("Convocatorias", seccion(tablaConvocatorias, null, null));
        add(pestanas, BorderLayout.CENTER);
        actualizar();
    }

    private JPanel seccion(JTable tabla, String textoBoton, java.awt.event.ActionListener accion) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        if (textoBoton != null) {
            JButton boton = Estilo.boton(textoBoton);
            boton.addActionListener(accion);
            panel.add(Estilo.barraDeBotones(boton), BorderLayout.SOUTH);
        }
        return panel;
    }

    public void actualizar() {
        List<Object[]> academias = new ArrayList<>();
        for (InscripcionDisciplina i : socio.getInscripciones()) {
            academias.add(new Object[] { i.getDisciplina(), i.getNivel(), i.getPuntajeRanking(), posicionDe(i) });
        }
        Estilo.llenar(tablaAcademias, academias);

        List<Object[]> reservas = new ArrayList<>();
        for (Reserva r : club.getReservas()) {
            if (r.getSocio() == socio) {
                reservas.add(new Object[] { r.getInstalacion().getNombre(), r.getFecha(), r.getHoraInicio(),
                        r.getHoraFin(), r.getNumeroJugadores(), r.getModalidad() });
            }
        }
        Estilo.llenar(tablaReservas, reservas);

        List<Object[]> practicas = new ArrayList<>();
        for (Partido p : club.getPartidos()) {
            if (p instanceof PartidoPractica && esMio((PartidoPractica) p)) {
                PartidoPractica practica = (PartidoPractica) p;
                practicas.add(new Object[] { practica.getDeporte(), practica.getFecha(), practica.getModalidad(),
                        nombres(practica), practica.resuelto() ? "Jugado" : "Programado" });
            }
        }
        Estilo.llenar(tablaPracticas, practicas);

        List<Object[]> convocatorias = new ArrayList<>();
        try {
            for (Convocatoria c : club.consultarConvocatorias(socio)) {
                convocatorias.add(new Object[] { c.getSesion().getFecha(), c.getSesion().getHora(),
                        c.getSesion().getInstalacion().getNombre(),
                        c.getAsistencia() == null ? "sin registrar" : c.getAsistencia(),
                        c.getNota() > 0 ? c.getNota() : "-", c.getComentario() == null ? "-" : c.getComentario() });
            }
        } catch (ReglaNegocioException e) {
        }
        Estilo.llenar(tablaConvocatorias, convocatorias);
    }

    private boolean esMio(PartidoPractica practica) {
        for (InscripcionDisciplina i : practica.getParticipantes()) {
            if (i.getSocio() == socio) {
                return true;
            }
        }
        return false;
    }

    private String nombres(PartidoPractica practica) {
        String texto = "";
        for (InscripcionDisciplina i : practica.getParticipantes()) {
            texto += (texto.isEmpty() ? "" : ", ") + i.getSocio().getNombre();
        }
        return texto;
    }

    private int posicionDe(InscripcionDisciplina inscripcion) {
        int mejores = 0;
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                for (InscripcionDisciplina otra : ((Socio) u).getInscripciones()) {
                    if (otra.getDisciplina() == inscripcion.getDisciplina()
                            && otra.getPuntajeRanking() > inscripcion.getPuntajeRanking()) {
                        mejores++;
                    }
                }
            }
        }
        return mejores + 1;
    }

    private void inscribirEnAcademia() {
        JComboBox<Deporte> disciplina = new JComboBox<>(INDIVIDUALES);
        JComboBox<Nivel> nivel = new JComboBox<>(Nivel.values());
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Disciplina"));
        formulario.add(disciplina);
        formulario.add(new JLabel("Nivel inicial"));
        formulario.add(nivel);
        if (JOptionPane.showConfirmDialog(this, formulario, "Inscribirme en una academia",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            club.inscribirEnAcademia(socio.getCodigo(), (Deporte) disciplina.getSelectedItem(),
                    (Nivel) nivel.getSelectedItem());
            Estilo.aviso(this, "Inscripcion exitosa. Su ELO inicial es 1000.");
            actualizar();
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private void reservar() {
        if (club.getInstalaciones().isEmpty()) {
            Estilo.error(this, new ReglaNegocioException("No hay instalaciones registradas"));
            return;
        }
        JComboBox<Instalacion> instalacion = new JComboBox<>(club.getInstalaciones().toArray(new Instalacion[0]));
        JTextField fecha = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField inicio = new JTextField("10:00");
        JTextField fin = new JTextField("11:00");
        JSpinner jugadores = new JSpinner(new SpinnerNumberModel(2, 1, 22, 1));
        JComboBox<Modalidad> modalidad = new JComboBox<>(Modalidad.values());
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Instalacion"));
        formulario.add(instalacion);
        formulario.add(new JLabel("Fecha (aaaa-mm-dd)"));
        formulario.add(fecha);
        formulario.add(new JLabel("Hora de inicio (hh:mm)"));
        formulario.add(inicio);
        formulario.add(new JLabel("Hora de fin (hh:mm)"));
        formulario.add(fin);
        formulario.add(new JLabel("Numero de jugadores"));
        formulario.add(jugadores);
        formulario.add(new JLabel("Modalidad"));
        formulario.add(modalidad);
        if (JOptionPane.showConfirmDialog(this, formulario, "Reservar una instalacion",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Reserva reserva = club.reservar(socio.getCodigo(), (Instalacion) instalacion.getSelectedItem(),
                    LocalDate.parse(fecha.getText().trim()), LocalTime.parse(inicio.getText().trim()),
                    LocalTime.parse(fin.getText().trim()), (Integer) jugadores.getValue(),
                    (Modalidad) modalidad.getSelectedItem());
            Estilo.aviso(this, "Reserva confirmada en " + reserva.getInstalacion().getNombre() + ".");
            actualizar();
        } catch (DateTimeParseException e) {
            Estilo.error(this, new ReglaNegocioException("Fecha u hora invalida. Ejemplo: 2026-12-15 y 14:30"));
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private void programarPractica() {
        if (socio.getInscripciones().isEmpty()) {
            Estilo.error(this, new ReglaNegocioException("Primero debe inscribirse en una academia"));
            return;
        }
        JComboBox<Deporte> mia = new JComboBox<>();
        for (InscripcionDisciplina i : socio.getInscripciones()) {
            mia.addItem(i.getDisciplina());
        }
        JComboBox<Modalidad> modalidad = new JComboBox<>(Modalidad.values());
        JTextField fecha = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField rivales = new JTextField();
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Mi disciplina"));
        formulario.add(mia);
        formulario.add(new JLabel("Modalidad"));
        formulario.add(modalidad);
        formulario.add(new JLabel("Fecha (aaaa-mm-dd)"));
        formulario.add(fecha);
        formulario.add(new JLabel("Usuarios de los demas jugadores, separados por coma (1 en sencillos, 3 en dobles)"));
        formulario.add(rivales);
        if (JOptionPane.showConfirmDialog(this, formulario, "Programar una practica",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Deporte deporte = (Deporte) mia.getSelectedItem();
            List<InscripcionDisciplina> participantes = new ArrayList<>();
            participantes.add(inscripcionDe(socio.getLogin(), deporte));
            for (String login : rivales.getText().split(",")) {
                if (!login.trim().isEmpty()) {
                    participantes.add(inscripcionDe(login.trim(), deporte));
                }
            }
            club.programarPractica(participantes, (Modalidad) modalidad.getSelectedItem(),
                    LocalDate.parse(fecha.getText().trim()));
            Estilo.aviso(this, "Practica programada.");
            actualizar();
        } catch (DateTimeParseException e) {
            Estilo.error(this, new ReglaNegocioException("Fecha invalida. Ejemplo: 2026-12-15"));
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private InscripcionDisciplina inscripcionDe(String login, Deporte deporte) {
        Usuario otro = null;
        for (Usuario u : club.getUsuarios()) {
            if (u.getLogin().equals(login)) {
                otro = u;
            }
        }
        if (!(otro instanceof Socio)) {
            throw new ReglaNegocioException(login + " no es un socio");
        }
        for (InscripcionDisciplina i : ((Socio) otro).getInscripciones()) {
            if (i.getDisciplina() == deporte) {
                return i;
            }
        }
        throw new ReglaNegocioException(login + " no esta inscrito en " + deporte);
    }
}
