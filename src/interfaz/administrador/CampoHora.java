package interfaz.administrador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class CampoHora extends JSpinner {

    private static final long serialVersionUID = 1L;

    public CampoHora(LocalTime inicial) {
        super(new SpinnerDateModel(convertir(inicial), null, null, Calendar.MINUTE));
        setEditor(new JSpinner.DateEditor(this, "HH:mm"));
        setPreferredSize(Estilo.ancho(80));
    }

    public LocalTime getHora() {
        Date hora = (Date) getValue();
        return hora.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private static Date convertir(LocalTime hora) {
        return Date.from(hora.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }
}
