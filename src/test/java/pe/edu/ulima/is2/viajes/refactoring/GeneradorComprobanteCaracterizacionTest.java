package pe.edu.ulima.is2.viajes.refactoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.reportes.GeneradorComprobante;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.ANA_VIP;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.JOHN_REGULAR;
import static pe.edu.ulima.is2.viajes.refactoring.Datos.LUIS_FRECUENTE;

/**
 * Pruebas de caracterización ("golden master") de GeneradorComprobante.
 * Documentan lo que el código HACE HOY. Son la red de seguridad de la Parte E:
 * deben pasar antes y después de cada refactoring.
 *
 * NO modifique los textos esperados. Si cambia la firma de un método durante el refactoring,
 * solo puede adaptar la forma de invocarlo.
 */
class GeneradorComprobanteCaracterizacionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 27);

    private GeneradorComprobante generador;
    private Reserva cuscoVipConSeguro;

    @BeforeEach
    void setUp() {
        generador = new GeneradorComprobante();
        cuscoVipConSeguro = Datos.confirmada(ANA_VIP, Datos.cusco(), 2, true, "2767.00", HOY.minusDays(7));
    }

    @Test
    @DisplayName("Boleta con detalle, monto > 700 (muestra documento), cliente VIP")
    void boletaConDetalle() {
        String esperado = """
                ========================================
                       AGENCIA DE VIAJES ULIMA TOURS
                            RUC 20123456789
                ========================================
                BOLETA DE VENTA ELECTRONICA B001-000001
                Fecha de emision: 27/11/2026
                Cliente: Ana Torres
                Documento: DNI 45678912
                ----------------------------------------
                Paquete: CUS-01 - Cusco y Machu Picchu
                Salida: 05/03/2027 (4 dias)
                Pasajeros: 2 x S/ 1450.00
                Incluye seguro de cancelacion
                ----------------------------------------
                TOTAL: S/ 2767.00
                ========================================
                Gracias por ser cliente VIP!
                """;
        assertEquals(esperado, generador.generar(cuscoVipConSeguro, "BOLETA", null, null, null, true, HOY));
    }

    @Test
    @DisplayName("Boleta sin detalle, monto <= 700 (no muestra documento), cliente regular")
    void boletaSinDetalle() {
        Reserva paracas = Datos.confirmada(JOHN_REGULAR, Datos.paracas(), 2, false, "640.00", HOY);
        String esperado = """
                ========================================
                       AGENCIA DE VIAJES ULIMA TOURS
                            RUC 20123456789
                ========================================
                BOLETA DE VENTA ELECTRONICA B001-000001
                Fecha de emision: 05/03/2026
                Cliente: John Smith
                ----------------------------------------
                TOTAL: S/ 640.00
                ========================================
                Gracias por su preferencia
                """;
        assertEquals(esperado, generador.generar(paracas, "BOLETA", null, null, null, false,
                LocalDate.of(2026, 3, 5)));
    }

    @Test
    @DisplayName("Boleta: el documento se muestra solo si el total es estrictamente mayor a 700")
    void boletaUmbralDocumento() {
        Reserva justo700 = Datos.confirmada(JOHN_REGULAR, Datos.paracas(), 2, false, "700.00", HOY);
        Reserva mas700 = Datos.confirmada(JOHN_REGULAR, Datos.paracas(), 2, false, "700.01", HOY);
        assertFalse(generador.generar(justo700, "BOLETA", null, null, null, false, HOY).contains("Documento:"));
        assertTrue(generador.generar(mas700, "BOLETA", null, null, null, false, HOY)
                .contains("Documento: PASAPORTE AB123456\n"));
    }

    @Test
    @DisplayName("Factura nacional con detalle y dirección: desglosa IGV")
    void facturaNacional() {
        String esperado = """
                ========================================
                       AGENCIA DE VIAJES ULIMA TOURS
                            RUC 20123456789
                ========================================
                FACTURA ELECTRONICA F001-000001
                Fecha de emision: 27/11/2026
                Senor(es): Minera Andina S.A.C.
                RUC: 20512345678
                Direccion: Av. Javier Prado 4600, Surco
                ----------------------------------------
                Paquete: CUS-01 - Cusco y Machu Picchu
                Salida: 05/03/2027 (4 dias)
                Pasajeros: 2 x S/ 1450.00
                Incluye seguro de cancelacion
                ----------------------------------------
                OP. GRAVADA: S/ 2344.92
                IGV (18%): S/ 422.08
                TOTAL: S/ 2767.00
                ========================================
                Gracias por ser cliente VIP!
                """;
        assertEquals(esperado, generador.generar(cuscoVipConSeguro, "FACTURA", "20512345678",
                "  Minera Andina S.A.C. ", " Av. Javier Prado 4600, Surco ", true, HOY));
    }

    @Test
    @DisplayName("Factura internacional sin detalle ni dirección: operación exonerada, cliente frecuente")
    void facturaInternacional() {
        Reserva cancun = Datos.confirmada(LUIS_FRECUENTE, Datos.cancun(), 3, false, "9312.00", HOY);
        String esperado = """
                ========================================
                       AGENCIA DE VIAJES ULIMA TOURS
                            RUC 20123456789
                ========================================
                FACTURA ELECTRONICA F001-000001
                Fecha de emision: 27/11/2026
                Senor(es): Luis Quispe
                RUC: 10456789123
                ----------------------------------------
                OP. EXONERADA: S/ 9312.00
                IGV (18%): S/ 0.00
                TOTAL: S/ 9312.00
                ========================================
                Gracias por viajar nuevamente con nosotros
                """;
        assertEquals(esperado, generador.generar(cancun, "FACTURA", "10456789123", "Luis Quispe", "  ",
                false, HOY));
    }

    @Test
    @DisplayName("Boletas y facturas llevan correlativos independientes")
    void correlativos() {
        generador.generar(cuscoVipConSeguro, "BOLETA", null, null, null, false, HOY);
        generador.generar(cuscoVipConSeguro, "FACTURA", "20512345678", "ACME", null, false, HOY);
        String segundaBoleta = generador.generar(cuscoVipConSeguro, "BOLETA", null, null, null, false, HOY);
        String segundaFactura = generador.generar(cuscoVipConSeguro, "FACTURA", "20512345678", "ACME", null,
                false, HOY);
        assertTrue(segundaBoleta.contains("B001-000002\n"));
        assertTrue(segundaFactura.contains("F001-000002\n"));
    }

    @Test
    @DisplayName("Errores: reserva cancelada, tipo desconocido, RUC inválido y razón social vacía")
    void errores() {
        Reserva cancelada = Datos.confirmada(ANA_VIP, Datos.cusco(), 1, false, "1450.00", HOY);
        cancelada.cancelar();
        assertThrows(IllegalStateException.class,
                () -> generador.generar(cancelada, "BOLETA", null, null, null, false, HOY));
        assertThrows(IllegalArgumentException.class,
                () -> generador.generar(cuscoVipConSeguro, "TICKET", null, null, null, false, HOY));
        for (String rucInvalido : new String[]{null, "2051234567", "205123456789", "30512345678"}) {
            assertThrows(IllegalArgumentException.class, () -> generador.generar(cuscoVipConSeguro, "FACTURA",
                    rucInvalido, "ACME", null, false, HOY), "RUC: " + rucInvalido);
        }
        assertThrows(IllegalArgumentException.class, () -> generador.generar(cuscoVipConSeguro, "FACTURA",
                "20512345678", "   ", null, false, HOY));
        assertThrows(IllegalArgumentException.class, () -> generador.generar(cuscoVipConSeguro, "FACTURA",
                "20512345678", null, null, false, HOY));
    }
}
