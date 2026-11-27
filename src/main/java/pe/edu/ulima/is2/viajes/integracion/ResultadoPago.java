package pe.edu.ulima.is2.viajes.integracion;

/**
 * Respuesta de la pasarela de pagos.
 *
 * @param aprobado           si la operación fue aprobada
 * @param codigoAutorizacion código de la operación (solo si fue aprobada)
 * @param mensaje            motivo del rechazo o mensaje informativo
 */
public record ResultadoPago(boolean aprobado, String codigoAutorizacion, String mensaje) {

    public static ResultadoPago aprobado(String codigoAutorizacion) {
        return new ResultadoPago(true, codigoAutorizacion, "Aprobado");
    }

    public static ResultadoPago rechazado(String motivo) {
        return new ResultadoPago(false, null, motivo);
    }
}
