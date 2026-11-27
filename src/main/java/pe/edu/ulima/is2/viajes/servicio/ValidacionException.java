package pe.edu.ulima.is2.viajes.servicio;

/** Se lanza cuando un dato ingresado no cumple la especificación. Indica el campo que falló. */
public class ValidacionException extends RuntimeException {

    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
