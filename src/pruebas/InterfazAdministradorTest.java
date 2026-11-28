package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import consola.DatosTorneos;
import interfaz.administrador.DatosAdministrador;
import interfaz.administrador.PanelIngresos;
import interfaz.administrador.PanelReportes;
import interfaz.administrador.PanelTorneos;
import interfaz.graficas.GraficaBarras;
import interfaz.graficas.SerieGrafica;
import modelo.Administrador;
import modelo.Club;
import modelo.IngresoDiario;
import modelo.RubroIngreso;
import modelo.Torneo;

class InterfazAdministradorTest {

    private Club club;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        club = new Club();
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        DatosTorneos.cargarSiHaceFalta(club);
        admin = (Administrador) DatosBase.usuario(club, DatosBase.ADMIN);
    }

    @Test
    void lasSeriesPorDiaRespetanElFiltroDeRubros() {
        DatosAdministrador.cargarVentasYMensualidades(club, LocalDate.now());
        List<IngresoDiario> dias = club.consultarIngresosNetos(admin.getCodigo(), LocalDate.now().minusDays(4));
        List<SerieGrafica> series = PanelIngresos.construirSeriesPorDia(dias,
                EnumSet.of(RubroIngreso.TIENDA, RubroIngreso.MENSUALIDADES));
        assertEquals(2, series.size());
        assertEquals("Tienda", series.get(0).getNombre());
        assertEquals("Mensualidades", series.get(1).getNombre());
        for (int i = 0; i < dias.size(); i++) {
            assertEquals(dias.get(i).getValor(RubroIngreso.TIENDA), series.get(0).getValor(i));
        }
    }

    @Test
    void laVistaAcumuladaSumaLosCincoDias() {
        DatosAdministrador.cargarVentasYMensualidades(club, LocalDate.now());
        List<IngresoDiario> dias = club.consultarIngresosNetos(admin.getCodigo(), LocalDate.now().minusDays(4));
        List<SerieGrafica> series = PanelIngresos.construirSeriesAcumuladas(dias, EnumSet.allOf(RubroIngreso.class));
        double esperado = 0;
        for (IngresoDiario dia : dias) {
            esperado += dia.getValor(RubroIngreso.CAFETERIA);
        }
        assertEquals(3, series.size());
        assertEquals(esperado, series.get(1).getValor(0), 0.01);
    }

    @Test
    void elPanelDeIngresosSeAlimentaDeLosDatosDelClub() {
        DatosAdministrador.cargarVentasYMensualidades(club, LocalDate.now());
        PanelIngresos panel = new PanelIngresos(club, admin);
        assertFalse(panel.getGrafica().estaVacia());
        assertEquals(Club.DIAS_REPORTE_INGRESOS, panel.getGrafica().getCategorias().size());
        panel.setRubroVisible(RubroIngreso.CAFETERIA, false);
        assertEquals(2, panel.getGrafica().getSeries().size());
        panel.setVistaAcumulada(true);
        assertEquals(1, panel.getGrafica().getCategorias().size());
    }

    @Test
    void sinMovimientosLaGraficaQuedaVacia() {
        PanelIngresos panel = new PanelIngresos(club, admin);
        panel.setInicio(LocalDate.of(2020, 1, 1));
        assertTrue(panel.getGrafica().estaVacia());
    }

    @Test
    void laGraficaRechazaSeriesIncompletas() {
        GraficaBarras grafica = new GraficaBarras();
        SerieGrafica corta = new SerieGrafica("A", java.awt.Color.BLUE, new double[] { 1 });
        assertThrows(IllegalArgumentException.class,
                () -> grafica.setDatos("t", Arrays.asList("x", "y"), new ArrayList<>(Arrays.asList(corta))));
    }

    @Test
    void elPanelDeTorneosMuestraLosTorneosDelClub() {
        PanelTorneos panel = new PanelTorneos(club, admin);
        Torneo primero = club.getTorneos().get(0);
        panel.seleccionar(primero);
        assertEquals(primero, panel.getTorneoSeleccionado());
    }

    @Test
    void elPanelDeReportesUsaElReporteDeVentasDelClub() {
        DatosAdministrador.cargarVentasYMensualidades(club, LocalDate.now());
        PanelReportes panel = new PanelReportes(club, admin);
        List<String> lineas = panel.consultar(PanelReportes.TipoReporte.VENTAS, LocalDate.now().minusDays(10),
                LocalDate.now());
        assertEquals(club.reporteVentas(admin.getCodigo(), LocalDate.now().minusDays(10), LocalDate.now()), lineas);
    }

    @Test
    void losDatosDeEjemploSoloSeCarganUnaVez() {
        DatosAdministrador.cargarSiHaceFalta(club);
        int ventas = club.getVentas().size();
        int sugerencias = club.consultarSugerencias(admin.getCodigo()).size();
        DatosAdministrador.cargarSiHaceFalta(club);
        assertEquals(ventas, club.getVentas().size());
        assertEquals(sugerencias, club.consultarSugerencias(admin.getCodigo()).size());
        assertTrue(ventas > 0);
    }
}
