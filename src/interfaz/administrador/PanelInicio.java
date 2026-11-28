package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import modelo.Administrador;
import modelo.Club;
import modelo.Equipo;
import modelo.EstadoSolicitud;
import modelo.EstadoTorneo;
import modelo.IngresoDiario;
import modelo.SolicitudCambioTurno;
import modelo.SugerenciaProducto;
import modelo.Torneo;

public class PanelInicio extends PanelBase {

    private static final long serialVersionUID = 1L;

    private final Consumer<String> navegar;
    private final JPanel tarjetas;
    private final DefaultListModel<String> pendientes;

    public PanelInicio(Club club, Administrador administrador, Consumer<String> navegar) {
        super(club, administrador, "Bienvenido(a), " + administrador.getNombre(),
                "Resumen del estado del club. Haga clic en una tarjeta para ir a la seccion.");
        this.navegar = navegar;
        tarjetas = new JPanel(new GridLayout(2, 4, 14, 14));
        tarjetas.setOpaque(false);
        pendientes = new DefaultListModel<>();
        JList<String> lista = new JList<>(pendientes);
        lista.setFont(Estilo.FUENTE_NORMAL);
        lista.setFixedCellHeight(26);

        JPanel bloquePendientes = new JPanel(new BorderLayout(0, 6));
        bloquePendientes.setOpaque(false);
        bloquePendientes.add(Estilo.subtitulo("Pendientes del administrador"), BorderLayout.NORTH);
        bloquePendientes.add(Estilo.scroll(lista), BorderLayout.CENTER);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 18));
        cuerpo.setOpaque(false);
        cuerpo.add(tarjetas, BorderLayout.NORTH);
        cuerpo.add(bloquePendientes, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        tarjetas.removeAll();
        pendientes.clear();
        List<SugerenciaProducto> sugerencias = new ArrayList<>();
        for (SugerenciaProducto s : club.consultarSugerencias(codigoAdministrador())) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                sugerencias.add(s);
            }
        }
        List<SolicitudCambioTurno> solicitudes = new ArrayList<>();
        for (SolicitudCambioTurno s : administrador.consultarSolicitudesCambioTurno(club)) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                solicitudes.add(s);
            }
        }
        int jugadores = 0;
        int sinEntrenador = 0;
        for (Equipo e : club.getEquipos()) {
            jugadores += e.getJugadores().size();
            if (e.getEntrenadorResponsable() == null) {
                sinEntrenador++;
            }
        }
        List<IngresoDiario> hoy = club.consultarIngresosNetos(codigoAdministrador(), LocalDate.now());
        double ingresosHoy = hoy.get(0).getTotal();

        agregarTarjeta("Torneos abiertos", club.consultarTorneosPorEstado(EstadoTorneo.ABIERTO).size(),
                VentanaAdministrador.TORNEOS);
        agregarTarjeta("Torneos en curso", club.consultarTorneosPorEstado(EstadoTorneo.EN_CURSO).size(),
                VentanaAdministrador.TORNEOS);
        agregarTarjeta("Equipos", club.getEquipos().size(), VentanaAdministrador.EQUIPOS);
        agregarTarjeta("Jugadores en equipos", jugadores, VentanaAdministrador.EQUIPOS);
        agregarTarjeta("Instalaciones", club.getInstalaciones().size(), VentanaAdministrador.INSTALACIONES);
        agregarTarjeta("Sugerencias pendientes", sugerencias.size(), VentanaAdministrador.INVENTARIO);
        agregarTarjeta("Cambios de turno pendientes", solicitudes.size(), VentanaAdministrador.TURNOS);
        agregarTarjeta("Ingresos netos de hoy", Estilo.dinero(ingresosHoy), VentanaAdministrador.INGRESOS);

        for (Torneo t : club.consultarTorneosPorEstado(EstadoTorneo.EN_CURSO)) {
            long faltan = t.getEncuentros().stream().filter(e -> !e.isJugado()).count();
            pendientes.addElement("Torneo " + t.getNombre() + ": " + faltan + " encuentro(s) sin resultado");
        }
        for (SolicitudCambioTurno s : solicitudes) {
            pendientes.addElement("Cambio de turno de " + s.getEmpleado().getNombre() + " a " + s.getNuevoDiaSemana()
                    + " " + s.getNuevaHoraInicio() + "-" + s.getNuevaHoraFin());
        }
        for (SugerenciaProducto s : sugerencias) {
            pendientes.addElement("Sugerencia de " + s.getEmpleado().getNombre() + ": " + s.getNombre());
        }
        if (sinEntrenador > 0) {
            pendientes.addElement(sinEntrenador + " equipo(s) sin entrenador responsable");
        }
        if (pendientes.isEmpty()) {
            pendientes.addElement("No hay tareas pendientes.");
        }
        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private void agregarTarjeta(String nombre, Object valor, String seccion) {
        JPanel tarjeta = Estilo.tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 6));
        JLabel numero = new JLabel(String.valueOf(valor));
        numero.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        numero.setForeground(Estilo.AZUL);
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setFont(Estilo.FUENTE_NORMAL);
        etiqueta.setForeground(Estilo.TEXTO_SUAVE);
        tarjeta.add(numero, BorderLayout.CENTER);
        tarjeta.add(etiqueta, BorderLayout.SOUTH);
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navegar.accept(seccion);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(Estilo.AZUL_CLARO);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }
        });
        tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Estilo.AZUL_MEDIO),
                tarjeta.getBorder()));
        tarjetas.add(tarjeta);
    }
}
