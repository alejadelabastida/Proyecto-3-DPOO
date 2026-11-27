package presentacion;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import modelo.Club;
import modelo.Usuario;

public class PanelPerfil extends JPanel {

    private static final long serialVersionUID = 1L;

    public PanelPerfil(Club club, Usuario usuario) {
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        add(Estilo.titulo("Mi perfil"), BorderLayout.NORTH);
        JTextArea texto = new JTextArea(club.consultarPerfil(usuario));
        texto.setEditable(false);
        texto.setFont(Estilo.NORMAL);
        texto.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(texto, BorderLayout.CENTER);
    }
}
