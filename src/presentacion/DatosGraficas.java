package presentacion;

import java.time.LocalDate;
import java.util.List;

import modelo.Club;
import modelo.EncuentroTorneo;
import modelo.EstadisticaPractica;
import modelo.InscripcionDisciplina;
import modelo.InscripcionTorneo;
import modelo.Partido;
import modelo.PartidoPractica;
import modelo.RegistroRanking;
import modelo.Torneo;
import modelo.Usuario;

public class DatosGraficas {

    public static final String TODAS = "Todas";

    private DatosGraficas() {
    }

    public static int eloAlFinalDe(InscripcionDisciplina inscripcion, LocalDate fin) {
        List<RegistroRanking> historial = inscripcion.getHistorial();
        if (historial.isEmpty()) {
            return inscripcion.getPuntajeRanking();
        }
        int elo = historial.get(0).getPuntajeAnterior();
        for (RegistroRanking registro : historial) {
            if (!registro.getFecha().isAfter(fin)) {
                elo = registro.getPuntajeNuevo();
            }
        }
        return elo;
    }

    public static int[] contarResultados(Club club, Usuario usuario, Object filtro, String tipo) {
        int[] conteo = new int[3];
        if (filtro == null || tipo == null) {
            return conteo;
        }
        if (!tipo.equals("Torneos")) {
            contarPracticas(club, usuario, filtro, conteo);
        }
        if (!tipo.equals("Practicas")) {
            contarTorneos(club, usuario, filtro, conteo);
        }
        return conteo;
    }

    private static void contarPracticas(Club club, Usuario usuario, Object filtro, int[] conteo) {
        for (Partido p : club.getPartidos()) {
            if (!(p instanceof PartidoPractica) || (!TODAS.equals(filtro) && filtro != p.getDeporte())) {
                continue;
            }
            PartidoPractica practica = (PartidoPractica) p;
            if (!practica.resuelto()) {
                continue;
            }
            List<InscripcionDisciplina> participantes = practica.getParticipantes();
            int mitad = participantes.size() / 2;
            int miPuesto = -1;
            for (int i = 0; i < participantes.size(); i++) {
                if (participantes.get(i).getSocio() == usuario) {
                    miPuesto = i;
                }
            }
            if (miPuesto < 0) {
                continue;
            }
            int setsPrimeros = 0;
            int setsSegundos = 0;
            for (EstadisticaPractica e : practica.getEstadisticasPractica()) {
                int puesto = participantes.indexOf(e.getInscripcion());
                if (puesto < mitad) {
                    setsPrimeros = e.getSets();
                } else {
                    setsSegundos = e.getSets();
                }
            }
            boolean ganoPrimera = setsPrimeros > setsSegundos;
            boolean soyPrimera = miPuesto < mitad;
            if (setsPrimeros == setsSegundos) {
                conteo[2]++;
            } else if (ganoPrimera == soyPrimera) {
                conteo[0]++;
            } else {
                conteo[1]++;
            }
        }
    }

    private static void contarTorneos(Club club, Usuario usuario, Object filtro, int[] conteo) {
        for (Torneo torneo : club.getTorneos()) {
            if (!TODAS.equals(filtro) && filtro != torneo.getDeporte()) {
                continue;
            }
            InscripcionTorneo mia = torneo.buscarInscripcionDe(usuario);
            if (mia == null) {
                continue;
            }
            for (EncuentroTorneo encuentro : torneo.getEncuentros()) {
                boolean juega = encuentro.getLocal() == mia || encuentro.getVisitante() == mia;
                if (!juega || !encuentro.isJugado()) {
                    continue;
                }
                InscripcionTorneo ganador = encuentro.getGanador();
                if (ganador == null) {
                    conteo[2]++;
                } else if (ganador == mia) {
                    conteo[0]++;
                } else {
                    conteo[1]++;
                }
            }
        }
    }
}
