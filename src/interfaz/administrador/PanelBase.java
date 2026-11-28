package interfaz.administrador;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import modelo.Administrador;
import modelo.Club;
import modelo.ReglaNegocioException;

public abstract class PanelBase extends JPanel implements Actualizable {

    private static final long serialVersionUID = 1L;

    protected final Club club;
    protected final Administrador administrador;

    protected PanelBase(Club club, Administrador administrador, String titulo, String descripcion) {
        super(new BorderLayout(0, 12));
        this.club = club;
        this.administrador = administrador;
        setBackground(Estilo.FONDO);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        encabezado.add(Estilo.titulo(titulo), BorderLayout.NORTH);
        JLabel detalle = new JLabel(descripcion);
        detalle.setForeground(Estilo.TEXTO_SUAVE);
        detalle.setFont(Estilo.FUENTE_NORMAL);
        encabezado.add(detalle, BorderLayout.SOUTH);
        add(encabezado, BorderLayout.NORTH);
    }

    protected boolean ejecutar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (ReglaNegocioException | IllegalArgumentException | IllegalStateException e) {
            Estilo.error(this, e.getMessage());
            return false;
        } finally {
            actualizar();
        }
    }

    protected int codigoAdministrador() {
        return administrador.getCodigo();
    }
}
