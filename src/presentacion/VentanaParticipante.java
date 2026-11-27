package presentacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import consola.DatosTorneos;
import modelo.Administrador;
import modelo.Club;
import modelo.Empleado;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.Usuario;
import persistencia.Persistencia;

public class VentanaParticipante extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String CARPETA_DATOS = "datos";
    private static final String PANTALLA_LOGIN = "login";
    private static final String PANTALLA_PRINCIPAL = "principal";

    private final Club club;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);
    private final JPanel pantallaPrincipal = new JPanel(new BorderLayout());
    private final JTextField campoUsuario = new JTextField(15);
    private final JPasswordField campoClave = new JPasswordField(15);

    public VentanaParticipante(Club club) {
        super("Club Deportivo Seneca - Participantes");
        this.club = club;
        contenedor.add(crearPantallaLogin(), PANTALLA_LOGIN);
        contenedor.add(pantallaPrincipal, PANTALLA_PRINCIPAL);
        add(contenedor);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                club.guardar();
                dispose();
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia(CARPETA_DATOS);
        Club club = persistencia.cargar();
        DatosTorneos.cargarSiHaceFalta(club);
        SwingUtilities.invokeLater(() -> new VentanaParticipante(club).setVisible(true));
    }

    // ---------------- Inicio de sesion y registro ----------------

    private JPanel crearPantallaLogin() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(60, 300, 60, 300));
        panel.setBackground(Estilo.AZUL_CLARO);

        JLabel titulo = new JLabel("Club Deportivo Seneca", SwingConstants.CENTER);
        titulo.setFont(Estilo.TITULO);
        titulo.setForeground(Estilo.AZUL);
        panel.add(titulo);
        panel.add(new JLabel("Usuario"));
        panel.add(campoUsuario);
        panel.add(new JLabel("Clave"));
        panel.add(campoClave);

        JButton entrar = Estilo.boton("Iniciar sesion");
        JButton estudiante = Estilo.boton("Registrarme como estudiante");
        JButton externo = Estilo.boton("Registrarme como persona externa");
        entrar.addActionListener(e -> iniciarSesion());
        campoClave.addActionListener(e -> iniciarSesion());
        estudiante.addActionListener(e -> registrar(true));
        externo.addActionListener(e -> registrar(false));
        panel.add(entrar);
        panel.add(estudiante);
        panel.add(externo);
        return panel;
    }

    private void iniciarSesion() {
        Usuario usuario = club.iniciarSesion(campoUsuario.getText().trim(), new String(campoClave.getPassword()));
        if (usuario == null) {
            Estilo.error(this, new ReglaNegocioException("Usuario o clave incorrectos"));
            return;
        }
        if (usuario instanceof Administrador || usuario instanceof Empleado) {
            Estilo.error(this, new ReglaNegocioException(
                    "Esta interfaz es para socios, estudiantes y externos. Use la de su rol."));
            return;
        }
        campoClave.setText("");
        mostrarPrincipal(usuario);
    }

    private void registrar(boolean esEstudiante) {
        JTextField nombre = new JTextField();
        JTextField apellido = new JTextField();
        JTextField dato = new JTextField();
        JTextField login = new JTextField();
        JPasswordField clave = new JPasswordField();
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Nombre"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido"));
        formulario.add(apellido);
        formulario.add(new JLabel(esEstudiante ? "Codigo estudiantil" : "Documento de identidad"));
        formulario.add(dato);
        formulario.add(new JLabel("Usuario nuevo"));
        formulario.add(login);
        formulario.add(new JLabel("Clave (minimo 4 caracteres)"));
        formulario.add(clave);
        if (JOptionPane.showConfirmDialog(this, formulario,
                esEstudiante ? "Registro de estudiante" : "Registro de persona externa",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String claveTexto = new String(clave.getPassword());
            if (esEstudiante) {
                club.registrarEstudiante(nombre.getText().trim(), apellido.getText().trim(),
                        login.getText().trim(), claveTexto, dato.getText().trim());
            } else {
                club.registrarExterno(nombre.getText().trim(), apellido.getText().trim(),
                        login.getText().trim(), claveTexto, dato.getText().trim());
            }
            Estilo.aviso(this, "Registro exitoso. Ya puede iniciar sesion.");
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    // ---------------- Pantalla principal ----------------

    private void mostrarPrincipal(Usuario usuario) {
        pantallaPrincipal.removeAll();

        JLabel saludo = new JLabel("  Bienvenido(a), " + usuario.getNombre() + " " + usuario.getApellido());
        saludo.setFont(Estilo.NORMAL);
        JButton salir = Estilo.boton("Cerrar sesion");
        salir.addActionListener(e -> {
            club.guardar();
            tarjetas.show(contenedor, PANTALLA_LOGIN);
        });
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Estilo.AZUL_CLARO);
        barra.add(saludo, BorderLayout.CENTER);
        barra.add(salir, BorderLayout.EAST);
        pantallaPrincipal.add(barra, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        PanelTorneos torneos = new PanelTorneos(club, usuario);
        pestanas.addTab("Torneos", torneos);
        if (usuario instanceof Socio) {
            Socio socio = (Socio) usuario;
            PanelDisciplinas disciplinas = new PanelDisciplinas(club, socio);
            PanelCompras compras = new PanelCompras(club, socio);
            PanelEstadisticas estadisticas = new PanelEstadisticas(club, socio);
            pestanas.addTab("Academias y reservas", disciplinas);
            pestanas.addTab("Compras", compras);
            pestanas.addTab("Graficas", estadisticas);
            pestanas.addChangeListener(e -> {
                torneos.actualizar();
                disciplinas.actualizar();
                compras.actualizar();
                estadisticas.recargarFiltros();
            });
        }
        pestanas.addTab("Perfil", new PanelPerfil(club, usuario));
        pantallaPrincipal.add(pestanas, BorderLayout.CENTER);

        pantallaPrincipal.revalidate();
        tarjetas.show(contenedor, PANTALLA_PRINCIPAL);
    }
}
