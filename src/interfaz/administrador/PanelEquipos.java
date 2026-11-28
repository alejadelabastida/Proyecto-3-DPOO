package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.Jugador;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.Usuario;

public class PanelEquipos extends PanelBase {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloEquipos;
    private final JTable tablaEquipos;
    private final DefaultTableModel modeloJugadores;
    private final JTable tablaJugadores;
    private final List<Equipo> equiposVisibles;
    private final List<Jugador> jugadoresVisibles;

    public PanelEquipos(Club club, Administrador administrador) {
        super(club, administrador, "Equipos y jugadores",
                "Cree equipos, asigne su entrenador responsable, inscriba socios y de de baja jugadores o equipos.");
        equiposVisibles = new ArrayList<>();
        jugadoresVisibles = new ArrayList<>();
        modeloEquipos = Estilo.modeloTabla("Equipo", "Deporte", "Categoria", "Jugadores", "Entrenador");
        tablaEquipos = Estilo.tabla(modeloEquipos);
        tablaEquipos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarJugadores();
            }
        });
        modeloJugadores = Estilo.modeloTabla("Camiseta", "Nombre", "Posicion", "Edad", "Estado");
        tablaJugadores = Estilo.tabla(modeloJugadores);

        JPanel izquierda = new JPanel(new BorderLayout(0, 6));
        izquierda.setOpaque(false);
        izquierda.add(Estilo.subtitulo("Equipos"), BorderLayout.NORTH);
        izquierda.add(Estilo.scroll(tablaEquipos), BorderLayout.CENTER);
        izquierda.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Crear equipo", e -> crearEquipo()),
                Estilo.boton("Asignar entrenador", e -> asignarEntrenador()),
                Estilo.boton("Dar de baja equipo", e -> darDeBajaEquipo())), BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new BorderLayout(0, 6));
        derecha.setOpaque(false);
        derecha.add(Estilo.subtitulo("Jugadores del equipo seleccionado"), BorderLayout.NORTH);
        derecha.add(Estilo.scroll(tablaJugadores), BorderLayout.CENTER);
        derecha.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Inscribir socio", e -> inscribirSocio()),
                Estilo.boton("Dar de baja jugador", e -> darDeBajaJugador())), BorderLayout.SOUTH);

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 16, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(izquierda);
        cuerpo.add(derecha);
        add(cuerpo, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        Equipo seleccionado = getEquipoSeleccionado();
        modeloEquipos.setRowCount(0);
        equiposVisibles.clear();
        for (Equipo equipo : club.getEquipos()) {
            equiposVisibles.add(equipo);
            Entrenador entrenador = equipo.getEntrenadorResponsable();
            modeloEquipos.addRow(new Object[] { equipo.getNombre(), equipo.getDeporte(), equipo.getCategoria(),
                    equipo.getJugadores().size() + "/" + equipo.getCupoMaximo(),
                    entrenador == null ? "Sin asignar" : entrenador.getNombre() + " " + entrenador.getApellido() });
        }
        int indice = equiposVisibles.indexOf(seleccionado);
        if (indice >= 0) {
            tablaEquipos.setRowSelectionInterval(indice, indice);
        } else if (!equiposVisibles.isEmpty()) {
            tablaEquipos.setRowSelectionInterval(0, 0);
        }
        mostrarJugadores();
    }

    public Equipo getEquipoSeleccionado() {
        int fila = tablaEquipos.getSelectedRow();
        return (fila >= 0 && fila < equiposVisibles.size()) ? equiposVisibles.get(fila) : null;
    }

    private void mostrarJugadores() {
        modeloJugadores.setRowCount(0);
        jugadoresVisibles.clear();
        Equipo equipo = getEquipoSeleccionado();
        if (equipo == null) {
            return;
        }
        for (Jugador j : equipo.getJugadores()) {
            jugadoresVisibles.add(j);
            modeloJugadores.addRow(new Object[] { j.getNumeroCamiseta(), j.getNombre() + " " + j.getApellido(),
                    j.getPosicion(), j.getEdad(), j.estaSuspendido() ? "Suspendido" : "Activo" });
        }
    }

    private Equipo exigirEquipo() {
        Equipo equipo = getEquipoSeleccionado();
        if (equipo == null) {
            throw new ReglaNegocioException("Seleccione primero un equipo");
        }
        return equipo;
    }

    private void crearEquipo() {
        JTextField nombre = new JTextField(20);
        JComboBox<Deporte> deporte = new JComboBox<>(
                new Deporte[] { Deporte.FUTBOL, Deporte.BALONCESTO, Deporte.VOLEIBOL });
        JComboBox<Categoria> categoria = new JComboBox<>(Categoria.values());
        JSpinner cupo = new JSpinner(new SpinnerNumberModel(15, 1, 40, 1));
        Formulario formulario = new Formulario()
                .agregar("Nombre", nombre)
                .agregar("Deporte", deporte)
                .agregar("Categoria", categoria)
                .agregar("Cupo maximo", cupo);
        formulario.mostrar(this, "Crear equipo", () -> {
            Equipo equipo = new Equipo(nombre.getText().trim(), (Deporte) deporte.getSelectedItem(),
                    (Categoria) categoria.getSelectedItem(), (Integer) cupo.getValue());
            club.crearEquipo(administrador, equipo);
            actualizar();
        });
    }

    private void asignarEntrenador() {
        ejecutar(() -> {
            Equipo equipo = exigirEquipo();
            List<Entrenador> entrenadores = new ArrayList<>();
            for (Usuario u : club.getUsuarios()) {
                if (u instanceof Entrenador) {
                    entrenadores.add((Entrenador) u);
                }
            }
            if (entrenadores.isEmpty()) {
                throw new ReglaNegocioException("No hay entrenadores registrados");
            }
            JComboBox<String> opciones = new JComboBox<>();
            for (Entrenador en : entrenadores) {
                opciones.addItem(en.getNombre() + " " + en.getApellido() + " (" + en.getLogin() + ")");
            }
            new Formulario().agregar("Entrenador", opciones).mostrar(this, "Asignar entrenador a " + equipo.getNombre(),
                    () -> club.asignarEntrenadorAEquipo(codigoAdministrador(), equipo,
                            entrenadores.get(opciones.getSelectedIndex()).getCodigo()));
        });
    }

    private void inscribirSocio() {
        ejecutar(() -> {
            Equipo equipo = exigirEquipo();
            List<Socio> socios = new ArrayList<>();
            for (Usuario u : club.getUsuarios()) {
                if (u instanceof Socio) {
                    socios.add((Socio) u);
                }
            }
            if (socios.isEmpty()) {
                throw new ReglaNegocioException("No hay socios registrados");
            }
            JComboBox<String> opciones = new JComboBox<>();
            for (Socio s : socios) {
                opciones.addItem(s.getNombre() + " " + s.getApellido() + " (" + s.getEdad() + " anos)");
            }
            JTextField posicion = new JTextField(14);
            JSpinner camiseta = new JSpinner(new SpinnerNumberModel(10, 1, 99, 1));
            Runnable precargar = () -> {
                Jugador registro = registroDeportivo(socios.get(opciones.getSelectedIndex()));
                if (registro != null) {
                    posicion.setText(registro.getPosicion());
                    camiseta.setValue(registro.getNumeroCamiseta());
                }
            };
            opciones.addActionListener(e -> precargar.run());
            precargar.run();
            new Formulario()
                    .agregar("Socio", opciones)
                    .agregar("Posicion", posicion)
                    .agregar("Numero de camiseta", camiseta)
                    .mostrar(this, "Inscribir socio en " + equipo.getNombre(), () -> {
                        if (posicion.getText().trim().isEmpty()) {
                            throw new ReglaNegocioException("Escriba la posicion del jugador");
                        }
                        club.inscribirJugador(socios.get(opciones.getSelectedIndex()).getCodigo(), equipo.getNombre(),
                                posicion.getText().trim(), (Integer) camiseta.getValue());
                    });
        });
    }

    private Jugador registroDeportivo(Socio socio) {
        if (socio instanceof Jugador) {
            return (Jugador) socio;
        }
        return socio.getJugadorDeportivo();
    }

    private void darDeBajaJugador() {
        ejecutar(() -> {
            int fila = tablaJugadores.getSelectedRow();
            if (fila < 0) {
                throw new ReglaNegocioException("Seleccione primero un jugador");
            }
            Jugador jugador = jugadoresVisibles.get(fila);
            if (Estilo.confirmar(this, "Dar de baja a " + jugador.getNombre() + " " + jugador.getApellido() + "?")) {
                club.darDeBajaJugador(administrador, jugador);
            }
        });
    }

    private void darDeBajaEquipo() {
        ejecutar(() -> {
            Equipo equipo = exigirEquipo();
            if (Estilo.confirmar(this, "Dar de baja el equipo " + equipo.getNombre() + "?")) {
                club.darDeBajaEquipo(administrador, equipo);
            }
        });
    }
}
