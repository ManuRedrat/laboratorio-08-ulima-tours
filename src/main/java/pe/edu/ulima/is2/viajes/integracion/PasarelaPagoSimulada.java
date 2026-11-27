package pe.edu.ulima.is2.viajes.integracion;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Simula una pasarela real: demora varios segundos y rechaza al azar algunas operaciones.
 * Por eso NO debe usarse en pruebas unitarias: hace que sean lentas y no deterministas.
 */
public class PasarelaPagoSimulada implements PasarelaPago {

    private final Random random = new Random();

    @Override
    public ResultadoPago cobrar(String numeroTarjeta, BigDecimal monto) {
        esperarRespuestaDelBanco();
        if (numeroTarjeta == null || !numeroTarjeta.matches("\\d{16}")) {
            return ResultadoPago.rechazado("Tarjeta inválida");
        }
        if (random.nextInt(10) == 0) {
            return ResultadoPago.rechazado("Fondos insuficientes");
        }
        return ResultadoPago.aprobado(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    @Override
    public ResultadoPago reembolsar(String codigoAutorizacion, BigDecimal monto) {
        esperarRespuestaDelBanco();
        return ResultadoPago.aprobado("R-" + codigoAutorizacion);
    }

    private void esperarRespuestaDelBanco() {
        try {
            Thread.sleep(1500 + random.nextInt(1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
