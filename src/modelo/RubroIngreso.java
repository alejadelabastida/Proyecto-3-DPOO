package modelo;

public enum RubroIngreso {
    TIENDA("Tienda"), CAFETERIA("Cafeteria"), MENSUALIDADES("Mensualidades");

    private final String etiqueta;

    RubroIngreso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
