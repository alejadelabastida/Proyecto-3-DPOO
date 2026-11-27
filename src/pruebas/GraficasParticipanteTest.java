package pruebas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.Club;
import modelo.Deporte;
import modelo.InscripcionDisciplina;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.PartidoPractica;
import modelo.Socio;
import presentacion.DatosGraficas;

public class GraficasParticipanteTest {

    private Club club;
    private Socio sofiaSocio;
    private Socio juanSocio;
    private InscripcionDisciplina sofia;
    private InscripcionDisciplina juan;

    @BeforeEach
    void preparar() {
        club = new Club();
        DatosBase.cargar(club);
        sofiaSocio = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        juanSocio = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        sofia = club.inscribirEnAcademia(DatosBase.SOCIO1, Deporte.TENIS, Nivel.INTERMEDIO);
        juan = club.inscribirEnAcademia(DatosBase.SOCIO2, Deporte.TENIS, Nivel.INTERMEDIO);
    }

    private Map<String, Integer> tenis(int sets) {
        Map<String, Integer> d = new HashMap<>();
        d.put("sets", sets);
        d.put("games", 6 * sets);
        d.put("aces", 1);
        d.put("erroresNoForzados", 2);
        return d;
    }

    private void jugarPractica(int setsSofia, int setsJuan) {
        List<InscripcionDisciplina> participantes = new ArrayList<>();
        participantes.add(sofia);
        participantes.add(juan);
        PartidoPractica p = club.programarPractica(participantes, Modalidad.SENCILLOS, LocalDate.now().minusDays(1));
        Map<InscripcionDisciplina, Map<String, Integer>> est = new HashMap<>();
        est.put(sofia, tenis(setsSofia));
        est.put(juan, tenis(setsJuan));
        club.registrarPractica(p, est);
    }

    @Test
    void eloSinHistorialEsElPuntajeActual() {
        assertEquals(sofia.getPuntajeRanking(), DatosGraficas.eloAlFinalDe(sofia, LocalDate.now()));
    }

    @Test
    void eloDependeDeLaFechaDeCorte() {
        int inicial = sofia.getPuntajeRanking();
        sofia.actualizarRanking(inicial + 20, LocalDate.of(2026, 10, 10));
        sofia.actualizarRanking(inicial + 35, LocalDate.of(2026, 10, 20));
        assertEquals(inicial, DatosGraficas.eloAlFinalDe(sofia, LocalDate.of(2026, 10, 5)));
        assertEquals(inicial + 20, DatosGraficas.eloAlFinalDe(sofia, LocalDate.of(2026, 10, 15)));
        assertEquals(inicial + 35, DatosGraficas.eloAlFinalDe(sofia, LocalDate.of(2026, 11, 1)));
    }

    @Test
    void pastelCuentaVictoriaYDerrota() {
        jugarPractica(2, 0);
        assertArrayEquals(new int[] { 1, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, Deporte.TENIS, "Todas"));
        assertArrayEquals(new int[] { 0, 1, 0 }, DatosGraficas.contarResultados(club, juanSocio, Deporte.TENIS, "Todas"));
    }

    @Test
    void pastelRespetaLosFiltros() {
        jugarPractica(2, 0);
        assertArrayEquals(new int[] { 1, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, DatosGraficas.TODAS, "Practicas"));
        assertArrayEquals(new int[] { 0, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, DatosGraficas.TODAS, "Torneos"));
        assertArrayEquals(new int[] { 0, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, Deporte.PADEL, "Todas"));
    }

    @Test
    void pastelSinFiltroDevuelveCeros() {
        jugarPractica(2, 0);
        assertArrayEquals(new int[] { 0, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, null, "Todas"));
    }

    @Test
    void pastelSinPartidosDevuelveCeros() {
        assertArrayEquals(new int[] { 0, 0, 0 }, DatosGraficas.contarResultados(club, sofiaSocio, DatosGraficas.TODAS, "Todas"));
    }
}
