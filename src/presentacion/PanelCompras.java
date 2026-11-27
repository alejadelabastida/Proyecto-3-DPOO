package presentacion;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import modelo.ArticuloTienda;
import modelo.Bono;
import modelo.Club;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.Snack;
import modelo.Socio;
import modelo.Venta;

public class PanelCompras extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Club club;
    private final Socio socio;
    private final JLabel puntos = new JLabel();
    private final JTable tablaBonos = Estilo.tabla("Bono", "Descuento", "Origen", "Estado");
    private final JTable tablaHistorial = Estilo.tabla("Fecha", "Detalle", "Subtotal", "Total", "Puntos ganados", "Puntos usados");

    public PanelCompras(Club club, Socio socio) {
        this.club = club;
        this.socio = socio;
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        add(Estilo.titulo("Compras, bonos y puntos"), BorderLayout.NORTH);

        JButton tienda = Estilo.boton("Comprar en la tienda");
        JButton cafeteria = Estilo.boton("Comprar en la cafeteria");
        tienda.addActionListener(e -> comprar(true));
        cafeteria.addActionListener(e -> comprar(false));
        puntos.setFont(Estilo.NORMAL);
        JPanel acciones = Estilo.barraDeBotones(tienda, cafeteria);
        acciones.add(puntos);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Mis bonos", Estilo.conScroll(tablaBonos));
        pestanas.addTab("Historial de compras", Estilo.conScroll(tablaHistorial));
        add(acciones, BorderLayout.SOUTH);
        add(pestanas, BorderLayout.CENTER);
        actualizar();
    }

    public void actualizar() {
        puntos.setText("   Puntos de fidelidad: " + socio.getPuntosFidelidad());

        List<Object[]> bonos = new ArrayList<>();
        int numero = 1;
        for (Bono b : club.consultarBonos(socio.getCodigo())) {
            bonos.add(new Object[] { "Bono " + numero++, Math.round(b.getPorcentaje() * 100) + "%",
                    b.getTorneoOrigen().getNombre(), b.isRedimido() ? "Usado" : "Disponible" });
        }
        Estilo.llenar(tablaBonos, bonos);

        List<Object[]> historial = new ArrayList<>();
        try {
            for (Venta v : club.consultarHistorialCompras(socio.getCodigo())) {
                String detalle = v.getClass().getSimpleName().replace("Venta", "");
                historial.add(new Object[] { v.getFecha().toLocalDate(), detalle, v.getSubtotal(), v.getTotal(),
                        v.getPuntosGanados(), v.getPuntosRedimidos() });
            }
        } catch (ReglaNegocioException e) {
        }
        Estilo.llenar(tablaHistorial, historial);
    }

    private void comprar(boolean esTienda) {
        List<Producto> productos = new ArrayList<>();
        for (Producto p : club.getProductos()) {
            if ((p instanceof ArticuloTienda) == esTienda) {
                productos.add(p);
            }
        }
        if (productos.isEmpty()) {
            Estilo.error(this, new ReglaNegocioException("No hay productos disponibles"));
            return;
        }

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 4));
        List<JSpinner> cantidades = new ArrayList<>();
        for (Producto p : productos) {
            String extra = (p instanceof Snack) ? " (alergenos: " + ((Snack) p).getAlergenos() + ")" : "";
            formulario.add(new JLabel(p.getNombre() + " - $" + (long) p.getPrecio() + extra));
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
            cantidades.add(cantidad);
            formulario.add(cantidad);
        }

        List<Bono> disponibles = new ArrayList<>();
        for (Bono b : club.consultarBonos(socio.getCodigo())) {
            if (!b.isRedimido()) {
                disponibles.add(b);
            }
        }
        JComboBox<String> bono = new JComboBox<>();
        bono.addItem("Sin bono");
        for (Bono b : disponibles) {
            bono.addItem(Math.round(b.getPorcentaje() * 100) + "% - " + b.getTorneoOrigen().getNombre());
        }
        JTextField codigo = new JTextField();
        JSpinner propina = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
        JSpinner puntosUsar = new JSpinner(new SpinnerNumberModel(0, 0, Math.max(0, socio.getPuntosFidelidad()), 10));
        formulario.add(new JLabel("Bono de descuento (no se acumula)"));
        formulario.add(bono);
        formulario.add(new JLabel("Codigo de descuento de un empleado"));
        formulario.add(codigo);
        if (!esTienda) {
            formulario.add(new JLabel("Propina (%)"));
            formulario.add(propina);
            formulario.add(new JLabel("Puntos de fidelidad a usar"));
            formulario.add(puntosUsar);
        }

        if (JOptionPane.showConfirmDialog(this, formulario, esTienda ? "Compra en la tienda" : "Compra en la cafeteria",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Map<Producto, Integer> pedido = new LinkedHashMap<>();
            for (int i = 0; i < productos.size(); i++) {
                int cantidad = (Integer) cantidades.get(i).getValue();
                if (cantidad > 0) {
                    pedido.put(productos.get(i), cantidad);
                }
            }
            if (pedido.isEmpty()) {
                throw new ReglaNegocioException("Debe elegir al menos un producto");
            }
            Bono elegido = bono.getSelectedIndex() == 0 ? null : disponibles.get(bono.getSelectedIndex() - 1);
            Venta venta = esTienda ? comprarTienda(pedido, elegido, codigo.getText().trim())
                    : comprarCafeteria(pedido, elegido, codigo.getText().trim(),
                            (Integer) propina.getValue() / 100.0, (Integer) puntosUsar.getValue());
            Estilo.aviso(this, "Compra realizada. Total: $" + (long) venta.getTotal());
            actualizar();
        } catch (ReglaNegocioException e) {
            Estilo.error(this, e);
        }
    }

    private Venta comprarTienda(Map<Producto, Integer> pedido, Bono bono, String codigo) {
        Map<ArticuloTienda, Integer> articulos = new LinkedHashMap<>();
        for (Map.Entry<Producto, Integer> e : pedido.entrySet()) {
            articulos.put((ArticuloTienda) e.getKey(), e.getValue());
        }
        if (bono != null) {
            return club.comprarTiendaConBono(socio.getCodigo(), articulos, bono);
        }
        return club.comprarTienda(socio.getCodigo(), articulos, codigo);
    }

    private Venta comprarCafeteria(Map<Producto, Integer> pedido, Bono bono, String codigo,
            double propina, int puntosUsar) {
        if (bono != null) {
            return club.comprarCafeteriaConBono(socio.getCodigo(), pedido, propina, bono);
        }
        return club.comprarCafeteria(socio.getCodigo(), pedido, propina, puntosUsar, codigo);
    }
}
