package interfaz.administrador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.ArticuloTienda;
import modelo.Club;
import modelo.Empleado;
import modelo.Mensualidad;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.Torneo;
import modelo.Turno;
import modelo.Usuario;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

public final class DatosAdministrador {

    public static final double VALOR_MENSUALIDAD = 120000;

    private static final int[][] VENTAS_TIENDA = { { 1, 0, 0 }, { 0, 2, 0 }, { 2, 1, 0 }, { 0, 0, 1 }, { 1, 1, 0 },
            { 3, 0, 0 }, { 0, 1, 1 } };
    private static final int[][] VENTAS_CAFETERIA = { { 4, 2, 3 }, { 2, 5, 1 }, { 6, 3, 4 }, { 3, 2, 2 },
            { 8, 4, 5 }, { 5, 6, 3 }, { 2, 3, 2 } };
    private static final int[] DIAS_MENSUALIDAD = { 6, 4, 2, 1 };

    private DatosAdministrador() {
    }

    public static void cargarSiHaceFalta(Club club) {
        boolean cambios = false;
        if (club.getVentas().isEmpty() && !hayMensualidades(club)) {
            cargarVentasYMensualidades(club, LocalDate.now());
            cambios = true;
        }
        if (cargarSugerencias(club)) {
            cambios = true;
        }
        if (cargarSolicitudes(club)) {
            cambios = true;
        }
        if (inscribirEnTorneoDeEjemplo(club)) {
            cambios = true;
        }
        if (cambios) {
            club.guardar();
        }
    }

    public static void cargarVentasYMensualidades(Club club, LocalDate hoy) {
        ArticuloTienda balon = (ArticuloTienda) DatosTienda.producto(club, "Balon");
        ArticuloTienda camiseta = (ArticuloTienda) DatosTienda.producto(club, "Camiseta");
        ArticuloTienda raqueta = (ArticuloTienda) DatosTienda.producto(club, "Raqueta");
        Producto agua = DatosTienda.producto(club, "Agua");
        Producto cafe = DatosTienda.producto(club, "Cafe");
        Producto galleta = DatosTienda.producto(club, "Galleta");
        Socio comprador = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        if (balon == null || camiseta == null || raqueta == null || agua == null || cafe == null || galleta == null
                || comprador == null) {
            return;
        }
        for (int i = 0; i < VENTAS_TIENDA.length; i++) {
            LocalDate dia = hoy.minusDays(VENTAS_TIENDA.length - 1 - i);
            Map<ArticuloTienda, Integer> articulos = new HashMap<>();
            agregar(articulos, balon, VENTAS_TIENDA[i][0]);
            agregar(articulos, camiseta, VENTAS_TIENDA[i][1]);
            agregar(articulos, raqueta, VENTAS_TIENDA[i][2]);
            if (!articulos.isEmpty()) {
                VentaTienda venta = new VentaTienda(comprador, articulos, 0);
                venta.setFecha(dia.atTime(LocalTime.of(11, 0)));
                club.getVentas().add(venta);
            }
            Map<Producto, Integer> consumo = new HashMap<>();
            agregar(consumo, agua, VENTAS_CAFETERIA[i][0]);
            agregar(consumo, cafe, VENTAS_CAFETERIA[i][1]);
            agregar(consumo, galleta, VENTAS_CAFETERIA[i][2]);
            VentaCafeteria venta = new VentaCafeteria(comprador, consumo, 0.0, 0, 0.0);
            venta.setFecha(dia.atTime(LocalTime.of(16, 30)));
            club.getVentas().add(venta);
        }
        int indice = 0;
        for (Usuario u : club.getUsuarios()) {
            if (u.getClass() == Socio.class && indice < DIAS_MENSUALIDAD.length) {
                LocalDate pago = hoy.minusDays(DIAS_MENSUALIDAD[indice]);
                new Mensualidad((Socio) u, YearMonth.from(pago).toString(), VALOR_MENSUALIDAD, pago);
                indice++;
            }
        }
    }

    private static <T extends Producto> void agregar(Map<T, Integer> mapa, T producto, int cantidad) {
        if (cantidad > 0) {
            mapa.put(producto, cantidad);
        }
    }

    private static boolean hayMensualidades(Club club) {
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio && !((Socio) u).getMensualidades().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean cargarSugerencias(Club club) {
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Empleado && !((Empleado) u).getSugerencias().isEmpty()) {
                return false;
            }
        }
        try {
            club.sugerirProducto(DatosBase.ENTRENADOR1, "Conos de entrenamiento", "Set de 20 conos para practicas");
            club.sugerirProducto(DatosBase.FISIO, "Bandas elasticas", "Para rehabilitacion de jugadores");
            return true;
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    private static boolean cargarSolicitudes(Club club) {
        if (!club.getSolicitudes().isEmpty()) {
            return false;
        }
        boolean agregadas = false;
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Empleado && !((Empleado) u).getTurnos().isEmpty()) {
                Empleado empleado = (Empleado) u;
                Turno turno = empleado.getTurnos().get(0);
                try {
                    club.solicitarCambioTurno(empleado.getCodigo(), turno, "Cita medica en la manana",
                            turno.getDiaSemana(), turno.getHoraInicio().plusHours(2), turno.getHoraFin().plusHours(2));
                    agregadas = true;
                } catch (ReglaNegocioException e) {
                    continue;
                }
                if (club.getSolicitudes().size() >= 2) {
                    break;
                }
            }
        }
        return agregadas;
    }

    private static boolean inscribirEnTorneoDeEjemplo(Club club) {
        for (Torneo torneo : club.getTorneos()) {
            if (torneo.getNombre().equals("Copa Tenis Intermedio") && torneo.getInscripciones().isEmpty()) {
                try {
                    club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, torneo);
                    club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, torneo);
                    return true;
                } catch (ReglaNegocioException e) {
                    return false;
                }
            }
        }
        return false;
    }
}
