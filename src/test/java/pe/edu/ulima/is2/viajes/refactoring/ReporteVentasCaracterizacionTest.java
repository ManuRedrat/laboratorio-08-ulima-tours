package pe.edu.ulima.is2.viajes.refactoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.reportes.ReporteVentas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.ANA_VIP;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.JOHN_REGULAR;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.LUIS_FRECUENTE;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.confirmada;

/**
 * Pruebas de caracterización ("golden master") de ReporteVentas. Ver indicaciones en
 * GeneradorComprobanteCaracterizacionTest.
 */
class ReporteVentasCaracterizacionTest {

    private final ReporteVentas reporte = new ReporteVentas();

    private List<Reserva> reservas() {
        Reserva cancelada = confirmada(LUIS_FRECUENTE, Datos.cusco(), 1, false, "1406.50", LocalDate.of(2026, 11, 26));
        cancelada.cancelar();
        Reserva pendiente = new Reserva(ANA_VIP, Datos.cancun(), 2, false, new BigDecimal("6400.00"),
                LocalDate.of(2026, 11, 10));
        return List.of(
                confirmada(ANA_VIP, Datos.cusco(), 2, true, "2767.00", LocalDate.of(2026, 11, 20)),
                confirmada(JOHN_REGULAR, Datos.paracas(), 2, false, "640.00", LocalDate.of(2026, 11, 21)),
                confirmada(LUIS_FRECUENTE, Datos.cancun(), 3, false, "9312.00", LocalDate.of(2026, 11, 22)),
                confirmada(JOHN_REGULAR, Datos.cusco(), 4, false, "5510.00", LocalDate.of(2026, 11, 25)),
                cancelada,
                confirmada(ANA_VIP, Datos.cancun(), 5, true, "14300.00", LocalDate.of(2026, 10, 30)),
                confirmada(ANA_VIP, Datos.cancun(), 1, false, "2976.00", LocalDate.of(2025, 11, 10)),
                pendiente);
    }

    @Test
    @DisplayName("Mes con ventas nacionales e internacionales; ignora canceladas, pendientes y otros meses/años")
    void mesConVentas() {
        String esperado = """
                REPORTE DE VENTAS 11/2026
                ----------------------------------------
                Nacional:      3 reservas, 8 pasajeros, S/ 8917.00
                Internacional: 1 reservas, 3 pasajeros, S/ 9312.00
                TOTAL:         4 reservas, 11 pasajeros, S/ 18229.00
                Comision vendedores: S/ 182.29
                Destino mas vendido: Cusco y Machu Picchu (6 pasajeros)
                """;
        assertEquals(esperado, reporte.generar(reservas(), 11, 2026));
    }

    @Test
    @DisplayName("Mes solo con ventas internacionales")
    void soloInternacional() {
        String esperado = """
                REPORTE DE VENTAS 10/2026
                ----------------------------------------
                Nacional:      0 reservas, 0 pasajeros, S/ 0.00
                Internacional: 1 reservas, 5 pasajeros, S/ 14300.00
                TOTAL:         1 reservas, 5 pasajeros, S/ 14300.00
                Comision vendedores: S/ 143.00
                Destino mas vendido: Cancun todo incluido (5 pasajeros)
                """;
        assertEquals(esperado, reporte.generar(reservas(), 10, 2026));
    }

    @Test
    @DisplayName("Mes sin ventas")
    void sinVentas() {
        String esperado = """
                REPORTE DE VENTAS 02/2026
                ----------------------------------------
                Nacional:      0 reservas, 0 pasajeros, S/ 0.00
                Internacional: 0 reservas, 0 pasajeros, S/ 0.00
                TOTAL:         0 reservas, 0 pasajeros, S/ 0.00
                Comision vendedores: S/ 0.00
                Destino mas vendido: -
                """;
        assertEquals(esperado, reporte.generar(reservas(), 2, 2026));
    }

    @Test
    @DisplayName("Tramos de comisión: 1% hasta 20000, 3% hasta 50000, 5% por encima")
    void tramosDeComision() {
        LocalDate dic = LocalDate.of(2026, 12, 2);
        assertTrue(reporte.generar(List.of(confirmada(JOHN_REGULAR, Datos.cusco(), 15, false, "20000.00", dic)),
                12, 2026).contains("Comision vendedores: S/ 200.00\n"));
        assertTrue(reporte.generar(List.of(confirmada(JOHN_REGULAR, Datos.cusco(), 15, false, "20000.01", dic)),
                12, 2026).contains("Comision vendedores: S/ 600.00\n"));
        assertTrue(reporte.generar(List.of(confirmada(ANA_VIP, Datos.cancun(), 20, false, "50000.00", dic)),
                12, 2026).contains("Comision vendedores: S/ 1500.00\n"));
        assertTrue(reporte.generar(List.of(confirmada(ANA_VIP, Datos.cancun(), 20, false, "60000.00", dic)),
                12, 2026).contains("Comision vendedores: S/ 3000.00\n"));
    }

    @Test
    @DisplayName("Empate en destino más vendido: gana el primero en orden alfabético")
    void empateDestino() {
        LocalDate dic = LocalDate.of(2026, 12, 2);
        String salida = reporte.generar(List.of(
                confirmada(JOHN_REGULAR, Datos.paracas(), 3, false, "960.00", dic),
                confirmada(ANA_VIP, Datos.cancun(), 3, false, "9600.00", dic)), 12, 2026);
        assertTrue(salida.endsWith("Destino mas vendido: Cancun todo incluido (3 pasajeros)\n"));
    }
}
