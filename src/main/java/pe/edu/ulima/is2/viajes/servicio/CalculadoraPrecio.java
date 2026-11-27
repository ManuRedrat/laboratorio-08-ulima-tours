package pe.edu.ulima.is2.viajes.servicio;

import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;

/** Calcula el monto total a pagar por una reserva. Las reglas están en ENUNCIADO.md (Parte D). */
public class CalculadoraPrecio {

    static final int MIN_PASAJEROS = 1;
    static final int MAX_PASAJEROS = 20;
    static final int RECARGO_TEMPORADA_ALTA = 15;
    static final int GRUPO_MEDIANO = 4;
    static final int GRUPO_GRANDE = 8;
    static final int DESCUENTO_MAXIMO = 15;
    static final BigDecimal SEGURO_NACIONAL = new BigDecimal("35.00");
    static final BigDecimal SEGURO_INTERNACIONAL = new BigDecimal("60.00");

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public BigDecimal calcular(PaqueteTuristico paquete, int pasajeros, CategoriaCliente categoria,
                               boolean conSeguro) {
        if (pasajeros < MIN_PASAJEROS || pasajeros > MAX_PASAJEROS) {
            throw new IllegalArgumentException("La cantidad de pasajeros debe estar entre "
                    + MIN_PASAJEROS + " y " + MAX_PASAJEROS);
        }
        BigDecimal subtotal = paquete.getPrecioPorPersona().multiply(BigDecimal.valueOf(pasajeros));
        if (esTemporadaAlta(paquete.getFechaSalida())) {
            subtotal = subtotal.add(porcentaje(subtotal, RECARGO_TEMPORADA_ALTA));
        }

        int descuento = descuentoPorGrupo(pasajeros) + descuentoPorCategoria(categoria);
        if (descuento > DESCUENTO_MAXIMO) {
            descuento = DESCUENTO_MAXIMO;
        }
        BigDecimal total = subtotal.subtract(porcentaje(subtotal, descuento));

        if (conSeguro) {
            total = total.add(costoSeguro(paquete.getTipoDestino()).multiply(BigDecimal.valueOf(pasajeros)));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    boolean esTemporadaAlta(LocalDate fechaSalida) {
        Month mes = fechaSalida.getMonth();
        return mes == Month.DECEMBER || mes == Month.JANUARY || mes == Month.FEBRUARY || mes == Month.JULY;
    }

    int descuentoPorGrupo(int pasajeros) {
        if (pasajeros >= GRUPO_GRANDE) {
            return 10;
        }
        if (pasajeros >= GRUPO_MEDIANO) {
            return 5;
        }
        return 0;
    }

    int descuentoPorCategoria(CategoriaCliente categoria) {
        return switch (categoria) {
            case VIP -> 7;
            case FRECUENTE -> 3;
            case REGULAR -> 0;
        };
    }

    private BigDecimal costoSeguro(TipoDestino tipoDestino) {
        return tipoDestino == TipoDestino.NACIONAL ? SEGURO_NACIONAL : SEGURO_INTERNACIONAL;
    }

    private BigDecimal porcentaje(BigDecimal monto, int porcentaje) {
        return monto.multiply(BigDecimal.valueOf(porcentaje)).divide(CIEN);
    }
}
