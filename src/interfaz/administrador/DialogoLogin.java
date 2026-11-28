package interfaz.administrador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import modelo.Administrador;
import modelo.Club;
import modelo.Usuario;

public class DialogoLogin extends JDialog {

    private static final long serialVersionUID = 1L;
    public static final int INTENTOS_PERMITIDOS = 3;

    private final Club club;
    private final JTextField campoUsuario;
    private final JPasswordField campoClave;
    private final JLabel mensaje;
    private int intentosFallidos;
    private Administrador administrador;

    public DialogoLogin(Club club) {
        super((java.awt.Frame) null, "Club Deportivo Seneca - Administrador", true);
        this.club = club;
        this.campoUsuario = new JTextField(16);
        this.campoClave = new JPasswordField(16);
        this.mensaje = new JLabel(" ");
        this.intentosFallidos = 0;
        construir();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static Administrador pedir(Club club) {
        DialogoLogin dialogo = new DialogoLogin(club);
        dialogo.setVisible(true);
        return dialogo.getAdministrador();
    }

    private void construir() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.AZUL);
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel titulo = new JLabel("Club Deportivo Seneca");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(Estilo.FUENTE_TITULO);
        JLabel subtitulo = new JLabel("Ingreso del administrador");
        subtitulo.setForeground(new Color(0xC9, 0xD5, 0xEC));
        subtitulo.setFont(Estilo.FUENTE_NORMAL);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(subtitulo, BorderLayout.SOUTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        formulario.add(new JLabel("Usuario"), c);
        c.gridx = 1;
        formulario.add(campoUsuario, c);
        c.gridx = 0;
        c.gridy = 1;
        formulario.add(new JLabel("Clave"), c);
        c.gridx = 1;
        formulario.add(campoClave, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        mensaje.setForeground(new Color(0xB3, 0x26, 0x1E));
        mensaje.setFont(Estilo.FUENTE_NORMAL.deriveFont(Font.PLAIN, 12f));
        formulario.add(mensaje, c);

        JButton ingresar = Estilo.botonPrincipal("Ingresar", e -> intentarIngreso());
        JButton salir = Estilo.boton("Salir", e -> dispose());
        JPanel botones = new JPanel();
        botones.setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));
        botones.add(ingresar);
        botones.add(salir);
        getRootPane().setDefaultButton(ingresar);

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void intentarIngreso() {
        String login = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());
        if (login.isEmpty() || clave.isEmpty()) {
            mensaje.setText("Escriba el usuario y la clave.");
            return;
        }
        Usuario usuario = club.iniciarSesion(login, clave);
        if (usuario instanceof Administrador) {
            administrador = (Administrador) usuario;
            dispose();
            return;
        }
        if (usuario != null) {
            Estilo.error(this, "Esta ventana es solo para el administrador. Use la aplicacion de su rol.");
            dispose();
            return;
        }
        intentosFallidos++;
        campoClave.setText("");
        if (intentosFallidos >= INTENTOS_PERMITIDOS) {
            Estilo.error(this, "Supero los " + INTENTOS_PERMITIDOS + " intentos. Se cierra el programa.");
            dispose();
            return;
        }
        mensaje.setText("Usuario o clave incorrectos. Intento " + intentosFallidos + " de " + INTENTOS_PERMITIDOS + ".");
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }
}
