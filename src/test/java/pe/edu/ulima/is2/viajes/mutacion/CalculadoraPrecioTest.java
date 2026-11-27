package pe.edu.ulima.is2.viajes.mutacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;
import pe.edu.ulima.is2.viajes.servicio.CalculadoraPrecio;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite heredada de otro equipo. Ejecuta todas las líneas de CalculadoraPrecio (revise JaCoCo),
 * pero... ¿realmente verifica algo? Use PIT para averiguarlo (Parte D).
 */
class CalculadoraPrecioTest {

    private final CalculadoraPrecio calculadora = new CalculadoraPrecio();

    private PaqueteTuristico paquete(TipoDestino tipo, LocalDate salida) {
        return new PaqueteTuristico("P-01", "Destino", tipo, salida, 5, new BigDecimal("1000.00"), 30);
    }

    @Test
    @DisplayName("Un pasajero regular en temporada baja")
    void unPasajeroRegular() {
        BigDecimal total = calculadora.calcular(paquete(TipoDestino.NACIONAL, LocalDate.of(2027, 5, 10)),
                1, CategoriaCliente.REGULAR, false);
        assertNotNull(total);
    }

    @Test
    @DisplayName("Temporada alta cuesta más")
    void temporadaAlta() {
        BigDecimal total = calculadora.calcular(paquete(TipoDestino.NACIONAL, LocalDate.of(2027, 1, 10)),
                2, CategoriaCliente.REGULAR, false);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Grupo mediano de un cliente frecuente")
    void grupoMediano() {
        BigDecimal total = calculadora.calcular(paquete(TipoDestino.NACIONAL, LocalDate.of(2027, 7, 10)),
                5, CategoriaCliente.FRECUENTE, false);
        assertTrue(total.compareTo(new BigDecimal("1000.00")) > 0);
    }

    @Test
    @DisplayName("Grupo grande VIP con seguro internacional")
    void grupoGrandeVip() {
        BigDecimal total = calculadora.calcular(paquete(TipoDestino.INTERNACIONAL, LocalDate.of(2027, 12, 1)),
                10, CategoriaCliente.VIP, true);
        assertNotNull(total);
    }

    @Test
    @DisplayName("Seguro nacional")
    void seguroNacional() {
        BigDecimal conSeguro = calculadora.calcular(paquete(TipoDestino.NACIONAL, LocalDate.of(2027, 2, 1)),
                3, CategoriaCliente.REGULAR, true);
        assertTrue(conSeguro.signum() > 0);
    }

    @Test
    @DisplayName("Más de 20 pasajeros no se permite")
    void demasiadosPasajeros() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcular(
                paquete(TipoDestino.NACIONAL, LocalDate.of(2027, 5, 10)), 25, CategoriaCliente.REGULAR, false));
    }
}
