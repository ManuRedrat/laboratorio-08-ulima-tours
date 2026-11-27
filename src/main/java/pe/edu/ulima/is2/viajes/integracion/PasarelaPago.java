package pe.edu.ulima.is2.viajes.integracion;

import java.math.BigDecimal;

/** Servicio externo de cobros con tarjeta (p. ej. Niubiz, Izipay, Culqi). */
public interface PasarelaPago {

    ResultadoPago cobrar(String numeroTarjeta, BigDecimal monto);

    /** Devuelve al cliente parte o todo lo cobrado en la operación indicada. */
    ResultadoPago reembolsar(String codigoAutorizacion, BigDecimal monto);
}
