package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.ArticuloTienda;
import modelo.Club;
import modelo.IngresoDiario;
import modelo.Mensualidad;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.RubroIngreso;
import modelo.Socio;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

class IngresosNetosTest {

    private Club club;
    private Socio juan;
    private LocalDate inicio;

    @BeforeEach
    void setUp() {
        club = new Club();
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        inicio = LocalDate.of(2026, 11, 2);
    }

    private VentaTienda ventaTienda(int balones, LocalDate dia) {
        Map<ArticuloTienda, Integer> articulos = new HashMap<>();
        articulos.put(DatosTienda.balon, balones);
        VentaTienda venta = new VentaTienda(juan, articulos, 0);
        venta.setFecha(dia.atTime(10, 0));
        club.getVentas().add(venta);
        return venta;
    }

    private VentaCafeteria ventaCafeteria(int cafes, LocalDate dia) {
        Map<Producto, Integer> productos = new HashMap<>();
        productos.put(DatosTienda.cafe, cafes);
        VentaCafeteria venta = new VentaCafeteria(juan, productos, 0.0, 0, 0.0);
        venta.setFecha(dia.atTime(15, 0));
        club.getVentas().add(venta);
        return venta;
    }

    @Test
    void elRangoSiempreTieneCincoDiasConsecutivos() {
        List<IngresoDiario> dias = club.consultarIngresosNetos(DatosBase.ADMIN, inicio);
        assertEquals(Club.DIAS_REPORTE_INGRESOS, dias.size());
        for (int i = 0; i < dias.size(); i++) {
            assertEquals(inicio.plusDays(i), dias.get(i).getFecha());
            assertEquals(0.0, dias.get(i).getTotal());
        }
    }

    @Test
    void laVentaDeTiendaSeSumaSinIva() {
        VentaTienda venta = ventaTienda(2, inicio);
        assertEquals(119000.0, venta.getTotal());
        List<IngresoDiario> dias = club.consultarIngresosNetos(DatosBase.ADMIN, inicio);
        assertEquals(100000.0, dias.get(0).getValor(RubroIngreso.TIENDA));
        assertEquals(19000.0, venta.getImpuestos());
    }

    @Test
    void laVentaDeCafeteriaSeSumaSinImpuestoAlConsumo() {
        VentaCafeteria venta = ventaCafeteria(10, inicio.plusDays(1));
        assertEquals(54000.0, venta.getTotal());
        List<IngresoDiario> dias = club.consultarIngresosNetos(DatosBase.ADMIN, inicio);
        assertEquals(50000.0, dias.get(1).getValor(RubroIngreso.CAFETERIA));
        assertEquals(0.0, dias.get(1).getValor(RubroIngreso.TIENDA));
    }

    @Test
    void lasMensualidadesSeSumanEnElDiaDelPago() {
        new Mensualidad(juan, "2026-11", 120000, inicio.plusDays(3));
        List<IngresoDiario> dias = club.consultarIngresosNetos(DatosBase.ADMIN, inicio);
        assertEquals(120000.0, dias.get(3).getValor(RubroIngreso.MENSUALIDADES));
    }

    @Test
    void losMovimientosFueraDelRangoNoSeCuentan() {
        ventaTienda(1, inicio.minusDays(1));
        ventaTienda(1, inicio.plusDays(Club.DIAS_REPORTE_INGRESOS));
        new Mensualidad(juan, "2026-10", 120000, inicio.minusDays(10));
        double total = 0;
        for (IngresoDiario dia : club.consultarIngresosNetos(DatosBase.ADMIN, inicio)) {
            total += dia.getTotal();
        }
        assertEquals(0.0, total);
    }

    @Test
    void elTotalDelDiaSumaLosTresRubros() {
        ventaTienda(1, inicio.plusDays(4));
        ventaCafeteria(2, inicio.plusDays(4));
        new Mensualidad(juan, "2026-11", 120000, inicio.plusDays(4));
        IngresoDiario dia = club.consultarIngresosNetos(DatosBase.ADMIN, inicio).get(4);
        assertEquals(50000.0 + 10000.0 + 120000.0, dia.getTotal());
    }

    @Test
    void soloElAdministradorConsultaLosIngresos() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> club.consultarIngresosNetos(DatosBase.SOCIO1, inicio));
        assertTrue(error.getMessage().contains("administrador"));
    }

    @Test
    void laFechaInicialEsObligatoria() {
        assertThrows(ReglaNegocioException.class, () -> club.consultarIngresosNetos(DatosBase.ADMIN, null));
    }
}
