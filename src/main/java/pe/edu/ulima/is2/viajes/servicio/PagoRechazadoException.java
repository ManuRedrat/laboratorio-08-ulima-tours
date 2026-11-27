package pe.edu.ulima.is2.viajes.servicio;

/** La pasarela de pagos rechazó un cobro o un reembolso. */
public class PagoRechazadoException extends ReservaException {

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
