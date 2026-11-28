package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import consola.DatosTorneos;
import modelo.Administrador;
import modelo.Club;
import persistencia.Persistencia;

public class VentanaAdministrador extends JFrame {

    private static final long serialVersionUID = 1L;
    public static final String CARPETA_DATOS = "datos";
    public static final String INICIO = "Inicio";
    public static final String TORNEOS = "Torneos";
    public static final String EQUIPOS = "Equipos y jugadores";
    public static final String INSTALACIONES = "Instalaciones";
    public static final String INVENTARIO = "Inventario y sugerencias";
    public static final String TURNOS = "Turnos del personal";
    public static final String COMPETENCIAS = "Competencias oficiales";
    public static final String REPORTES = "Consultas y reportes";
    public static final String INGRESOS = "Grafica de ingresos";

    private final Club club;
    private final Administrador administrador;
    private final CardLayout tarjetas;
    private final JPanel contenido;
    private final JPanel menu;
    private final ButtonGroup grupoMenu;
    private final Map<String, PanelBase> secciones;
    private final Map<String, JToggleButton> botonesMenu;
    private String seccionActual;

    public VentanaAdministrador(Club club, Administrador administrador) {
        super("Club Deportivo Seneca - Administrador");
        this.club = club;
        this.administrador = administrador;
        this.tarjetas = new CardLayout();
        this.contenido = new JPanel(tarjetas);
        this.menu = new JPanel(new GridLayout(0, 1, 0, 2));
        this.grupoMenu = new ButtonGroup();
        this.secciones = new LinkedHashMap<>();
        this.botonesMenu = new LinkedHashMap<>();
        construir();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        mostrar(INICIO);
    }

    public static void main(String[] args) {
        configurarApariencia();
        SwingUtilities.invokeLater(() -> {
            Club club = new Persistencia(CARPETA_DATOS).cargar();
            DatosTorneos.cargarSiHaceFalta(club);
            DatosAdministrador.cargarSiHaceFalta(club);
            iniciarSesion(club);
        });
    }

    public static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("nimbusBase", Estilo.AZUL);
                    UIManager.put("nimbusFocus", Estilo.AZUL_MEDIO);
                    UIManager.put("nimbusSelectionBackground", Estilo.AZUL_MEDIO);
                    UIManager.put("Table.alternateRowColor", new Color(0xF7, 0xF9, 0xFC));
                    return;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            return;
        }
    }

    public static void iniciarSesion(Club club) {
        Administrador administrador = DialogoLogin.pedir(club);
        if (administrador == null) {
            club.guardar();
            System.exit(0);
            return;
        }
        new VentanaAdministrador(club, administrador).setVisible(true);
    }

    private void construir() {
        setLayout(new BorderLayout());
        add(crearEncabezado(), BorderLayout.NORTH);
        menu.setBackground(Estilo.AZUL);
        menu.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JPanel contenedorMenu = new JPanel(new BorderLayout());
        contenedorMenu.setBackground(Estilo.AZUL);
        contenedorMenu.setPreferredSize(new Dimension(230, 100));
        contenedorMenu.add(menu, BorderLayout.NORTH);
        add(contenedorMenu, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);

        agregarSeccion(INICIO, new PanelInicio(club, administrador, this::mostrar));
        agregarSeccion(TORNEOS, new PanelTorneos(club, administrador));
        agregarSeccion(EQUIPOS, new PanelEquipos(club, administrador));
        agregarSeccion(INSTALACIONES, new PanelInstalaciones(club, administrador));
        agregarSeccion(INVENTARIO, new PanelInventario(club, administrador));
        agregarSeccion(TURNOS, new PanelTurnos(club, administrador));
        agregarSeccion(COMPETENCIAS, new PanelCompetencias(club, administrador));
        agregarSeccion(REPORTES, new PanelReportes(club, administrador));
        agregarSeccion(INGRESOS, new PanelIngresos(club, administrador));
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        JLabel nombreClub = new JLabel("Club Deportivo Seneca");
        nombreClub.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        nombreClub.setForeground(Estilo.AZUL);
        JLabel rol = new JLabel("Panel del administrador");
        rol.setForeground(Estilo.TEXTO_SUAVE);
        JPanel izquierda = new JPanel(new GridLayout(2, 1));
        izquierda.setOpaque(false);
        izquierda.add(nombreClub);
        izquierda.add(rol);

        JLabel usuario = new JLabel(administrador.getNombre() + " " + administrador.getApellido() + "  ("
                + administrador.getLogin() + ")");
        usuario.setFont(Estilo.FUENTE_NORMAL);
        JButton cerrarSesion = Estilo.boton("Cerrar sesion", e -> cerrarSesion());
        JButton salir = Estilo.boton("Salir", e -> salir());
        JPanel derecha = Estilo.barraBotones(usuario, cerrarSesion, salir);
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    private void agregarSeccion(String nombre, PanelBase panel) {
        secciones.put(nombre, panel);
        contenido.add(panel, nombre);
        JToggleButton boton = new JToggleButton(nombre);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setFont(Estilo.FUENTE_NORMAL);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(200, 38));
        boton.addActionListener(e -> mostrar(nombre));
        grupoMenu.add(boton);
        botonesMenu.put(nombre, boton);
        menu.add(boton);
    }

    public void mostrar(String nombre) {
        PanelBase panel = secciones.get(nombre);
        if (panel == null) {
            return;
        }
        seccionActual = nombre;
        panel.actualizar();
        tarjetas.show(contenido, nombre);
        botonesMenu.get(nombre).setSelected(true);
    }

    public String getSeccionActual() {
        return seccionActual;
    }

    public PanelBase getSeccion(String nombre) {
        return secciones.get(nombre);
    }

    private void cerrarSesion() {
        if (Estilo.confirmar(this, "Desea cerrar la sesion? Los datos se guardan automaticamente.")) {
            club.guardar();
            dispose();
            SwingUtilities.invokeLater(() -> iniciarSesion(club));
        }
    }

    private void salir() {
        club.guardar();
        dispose();
        System.exit(0);
    }
}
