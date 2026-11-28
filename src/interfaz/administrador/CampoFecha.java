package interfaz.administrador;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class CampoFecha extends JSpinner {

    private static final long serialVersionUID = 1L;

    public CampoFecha(LocalDate inicial) {
        super(new SpinnerDateModel(convertir(inicial), null, null, Calendar.DAY_OF_MONTH));
        setEditor(new JSpinner.DateEditor(this, "yyyy-MM-dd"));
        setPreferredSize(Estilo.ancho(120));
    }

    public LocalDate getFecha() {
        Date fecha = (Date) getValue();
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setFecha(LocalDate fecha) {
        setValue(convertir(fecha));
    }

    private static Date convertir(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
