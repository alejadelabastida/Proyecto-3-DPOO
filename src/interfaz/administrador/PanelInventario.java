package interfaz.administrador;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import modelo.Administrador;
import modelo.ArticuloTienda;
import modelo.Bebida;
import modelo.Club;
import modelo.EstadoSolicitud;
import modelo.ExistenciaInventario;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.SugerenciaProducto;
import modelo.UbicacionInventario;

public class PanelInventario extends PanelBase {

    private static final long serialVersionUID = 1L;
    private static final String TODAS = "Todas";

    private final JComboBox<String> filtroUbicacion;
    private final DefaultTableModel modeloInventario;
    private final JCheckBox soloPendientes;
    private final DefaultTableModel modeloSugerencias;
    private final JTable tablaSugerencias;
    private final List<SugerenciaProducto> sugerenciasVisibles;

    public PanelInventario(Club club, Administrador administrador) {
        super(club, administrador, "Inventario y sugerencias",
                "Revise las existencias por ubicacion, reabastezca la tienda, mueva articulos y evalue sugerencias.");
        sugerenciasVisibles = new ArrayList<>();
        filtroUbicacion = new JComboBox<>();
        filtroUbicacion.addActionListener(e -> mostrarInventario());
        modeloInventario = Estilo.modeloTabla("Ubicacion", "Producto", "Tipo", "Precio", "Cantidad");
        soloPendientes = new JCheckBox("Solo pendientes", true);
        soloPendientes.setOpaque(false);
        soloPendientes.addActionListener(e -> mostrarSugerencias());
        modeloSugerencias = Estilo.modeloTabla("Producto sugerido", "Descripcion", "Empleado", "Estado");
        tablaSugerencias = Estilo.tabla(modeloSugerencias);

        JPanel inventario = new JPanel(new BorderLayout(0, 6));
        inventario.setOpaque(false);
        inventario.add(Estilo.barraBotones(Estilo.subtitulo("Inventario"), new JLabel("   Ubicacion:"), filtroUbicacion),
                BorderLayout.NORTH);
        inventario.add(Estilo.scroll(Estilo.tabla(modeloInventario)), BorderLayout.CENTER);
        inventario.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Reabastecer tienda", e -> reabastecer()),
                Estilo.boton("Mover entre ubicaciones", e -> mover())), BorderLayout.SOUTH);

        JPanel sugerencias = new JPanel(new BorderLayout(0, 6));
        sugerencias.setOpaque(false);
        sugerencias.add(Estilo.barraBotones(Estilo.subtitulo("Sugerencias de los empleados"), soloPendientes),
                BorderLayout.NORTH);
        sugerencias.add(Estilo.scroll(tablaSugerencias), BorderLayout.CENTER);
        sugerencias.add(Estilo.barraBotones(
                Estilo.botonPrincipal("Aprobar", e -> evaluar(true)),
                Estilo.boton("Rechazar", e -> evaluar(false))), BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inventario, sugerencias);
        division.setResizeWeight(0.55);
        division.setBorder(null);
        division.setOpaque(false);
        add(division, BorderLayout.CENTER);
        actualizar();
    }

    @Override
    public void actualizar() {
        Object anterior = filtroUbicacion.getSelectedItem();
        filtroUbicacion.removeAllItems();
        filtroUbicacion.addItem(TODAS);
        for (UbicacionInventario u : club.getUbicaciones()) {
            filtroUbicacion.addItem(u.getNombre());
        }
        if (anterior != null) {
            filtroUbicacion.setSelectedItem(anterior);
        }
        mostrarInventario();
        mostrarSugerencias();
    }

    private void mostrarInventario() {
        modeloInventario.setRowCount(0);
        Object filtro = filtroUbicacion.getSelectedItem();
        for (UbicacionInventario u : club.getUbicaciones()) {
            if (filtro != null && !TODAS.equals(filtro) && !u.getNombre().equals(filtro)) {
                continue;
            }
            for (ExistenciaInventario e : u.getExistencias()) {
                Producto p = e.getProducto();
                modeloInventario.addRow(new Object[] { u.getNombre(), p.getNombre(), tipo(p),
                        Estilo.dinero(p.getPrecio()), e.getCantidad() });
            }
        }
    }

    private void mostrarSugerencias() {
        modeloSugerencias.setRowCount(0);
        sugerenciasVisibles.clear();
        for (SugerenciaProducto s : club.consultarSugerencias(codigoAdministrador())) {
            if (soloPendientes.isSelected() && s.getEstado() != EstadoSolicitud.PENDIENTE) {
                continue;
            }
            sugerenciasVisibles.add(s);
            modeloSugerencias.addRow(new Object[] { s.getNombre(), s.getDescripcion(),
                    s.getEmpleado().getNombre() + " " + s.getEmpleado().getApellido(), s.getEstado() });
        }
    }

    private String tipo(Producto p) {
        if (p instanceof ArticuloTienda) {
            return "Articulo de tienda";
        }
        if (p instanceof Bebida) {
            return "Bebida";
        }
        return "Snack";
    }

    private void reabastecer() {
        ejecutar(() -> {
            List<ArticuloTienda> articulos = new ArrayList<>();
            for (Producto p : club.getProductos()) {
                if (p instanceof ArticuloTienda) {
                    articulos.add((ArticuloTienda) p);
                }
            }
            if (articulos.isEmpty()) {
                throw new ReglaNegocioException("No hay articulos de tienda registrados");
            }
            JComboBox<String> opciones = new JComboBox<>();
            for (ArticuloTienda a : articulos) {
                opciones.addItem(a.getNombre() + " (" + Estilo.dinero(a.getPrecio()) + ")");
            }
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
            new Formulario().agregar("Articulo", opciones).agregar("Cantidad a agregar", cantidad)
                    .mostrar(this, "Reabastecer la tienda", () -> club.reabastecer(codigoAdministrador(),
                            articulos.get(opciones.getSelectedIndex()), (Integer) cantidad.getValue()));
        });
    }

    private void mover() {
        ejecutar(() -> {
            List<Producto> productos = club.getProductos();
            List<UbicacionInventario> ubicaciones = club.getUbicaciones();
            if (productos.isEmpty() || ubicaciones.size() < 2) {
                throw new ReglaNegocioException("Se necesitan productos y al menos dos ubicaciones");
            }
            JComboBox<String> producto = new JComboBox<>();
            for (Producto p : productos) {
                producto.addItem(p.getNombre());
            }
            JComboBox<String> origen = new JComboBox<>();
            JComboBox<String> destino = new JComboBox<>();
            for (UbicacionInventario u : ubicaciones) {
                origen.addItem(u.getNombre());
                destino.addItem(u.getNombre());
            }
            destino.setSelectedIndex(1);
            JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
            new Formulario().agregar("Producto", producto).agregar("Origen", origen).agregar("Destino", destino)
                    .agregar("Cantidad", cantidad)
                    .mostrar(this, "Mover articulos", () -> club.moverArticulo(codigoAdministrador(),
                            productos.get(producto.getSelectedIndex()), (String) origen.getSelectedItem(),
                            (String) destino.getSelectedItem(), (Integer) cantidad.getValue()));
        });
    }

    private void evaluar(boolean aprobar) {
        ejecutar(() -> {
            int fila = tablaSugerencias.getSelectedRow();
            if (fila < 0) {
                throw new ReglaNegocioException("Seleccione primero una sugerencia");
            }
            SugerenciaProducto sugerencia = sugerenciasVisibles.get(fila);
            if (aprobar) {
                club.aprobarSugerencia(codigoAdministrador(), sugerencia);
            } else {
                club.rechazarSugerencia(codigoAdministrador(), sugerencia);
            }
        });
    }
}
