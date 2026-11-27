package pe.edu.ulima.is2.viajes.mocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.ulima.is2.viajes.integracion.Notificador;
import pe.edu.ulima.is2.viajes.integracion.PasarelaPago;
import pe.edu.ulima.is2.viajes.integracion.ResultadoPago;
import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.EstadoReserva;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepository;
import pe.edu.ulima.is2.viajes.repositorio.PaqueteRepository;
import pe.edu.ulima.is2.viajes.repositorio.ReservaRepository;
import pe.edu.ulima.is2.viajes.servicio.CalculadoraPrecio;
import pe.edu.ulima.is2.viajes.servicio.PoliticaCancelacion;
import pe.edu.ulima.is2.viajes.servicio.ReservaService;
import pe.edu.ulima.is2.viajes.servicio.ReservaException;
import pe.edu.ulima.is2.viajes.servicio.PagoRechazadoException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Pruebas unitarias de ReservaService aislando TODAS sus dependencias con stubs de Mockito (Parte C).
 * El caso MK01 es un ejemplo resuelto: úselo como modelo para el resto.
 *
 * Solo se programan respuestas (when/thenReturn, thenAnswer, thenThrow, doThrow) y se verifica el
 * RESULTADO observable: valor devuelto, excepción lanzada y estado de los objetos.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 27);

    @Mock private ClienteRepository clienteRepository;
    @Mock private PaqueteRepository paqueteRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private CalculadoraPrecio calculadoraPrecio;
    @Mock private PoliticaCancelacion politicaCancelacion;
    @Mock private PasarelaPago pasarelaPago;
    @Mock private Notificador notificador;

    private ReservaService service;
    private Cliente cliente;
    private PaqueteTuristico paquete;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new ReservaService(clienteRepository, paqueteRepository, reservaRepository, calculadoraPrecio,
                politicaCancelacion, pasarelaPago, notificador, reloj);
        cliente = new Cliente(TipoDocumento.DNI, "45678912", "Ana Torres", "ana@correo.pe",
                LocalDate.of(1990, 5, 12), CategoriaCliente.FRECUENTE);
        paquete = new PaqueteTuristico("CUS-01", "Cusco", TipoDestino.NACIONAL, HOY.plusDays(40), 4,
                new BigDecimal("1450.00"), 10);
    }

    @Test
    @DisplayName("MK01 - Reserva exitosa: cobra el monto calculado, confirma la reserva y notifica")
    void reservaExitosa() {
        // Arrange: se programa (stubbing) la respuesta de cada dependencia
        when(clienteRepository.buscarPorDocumento(TipoDocumento.DNI, "45678912")).thenReturn(Optional.of(cliente));
        when(paqueteRepository.buscarPorCodigo("CUS-01")).thenReturn(Optional.of(paquete));
        when(calculadoraPrecio.calcular(paquete, 2, CategoriaCliente.FRECUENTE, false))
                .thenReturn(new BigDecimal("2813.00"));
        when(pasarelaPago.cobrar("4111111111111111", new BigDecimal("2813.00")))
                .thenReturn(ResultadoPago.aprobado("AUT123"));
        when(reservaRepository.guardar(any(Reserva.class))).thenAnswer(inv -> {
            Reserva guardada = inv.getArgument(0);   // el stub simula que la BD asigna el id
            guardada.setId(1L);
            return guardada;
        });

        // Act
        Reserva reserva = service.reservar(TipoDocumento.DNI, "45678912", "CUS-01", 2, false, "4111111111111111");

        // Assert: resultado y estado observable
        assertEquals(1L, reserva.getId());                       // pasó por reservaRepository.guardar
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals("AUT123", reserva.getCodigoAutorizacion());  // viene del stub de la pasarela
        assertEquals(new BigDecimal("2813.00"), reserva.getMontoTotal());
        assertEquals(HOY, reserva.getFechaReserva());             // viene del reloj fijo
        assertEquals(8, paquete.getCuposDisponibles());           // 10 - 2 pasajeros
    }

    private Reserva reservar() { return service.reservar(TipoDocumento.DNI, "45678912", "CUS-01", 2, false, "4111111111111111"); }
    private void clienteExiste() { when(clienteRepository.buscarPorDocumento(TipoDocumento.DNI, "45678912")).thenReturn(Optional.of(cliente)); }
    private void paqueteExiste() { clienteExiste(); when(paqueteRepository.buscarPorCodigo("CUS-01")).thenReturn(Optional.of(paquete)); }
    private Reserva reservaConfirmada() {
        Reserva r = new Reserva(cliente, paquete, 2, false, new BigDecimal("101.01"), HOY.minusDays(1));
        r.setId(1L); r.confirmar("AUT123"); return r;
    }
    @Test @DisplayName("MK02 RN1: cliente no registrado") void clienteAusente() {
        assertThrows(ReservaException.class, this::reservar); assertEquals(10, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK03 RN2: paquete no registrado") void paqueteAusente() {
        clienteExiste(); assertThrows(ReservaException.class, this::reservar);
    }
    @Test @DisplayName("MK04 RN3: salida hoy no admite reservas") void salidaHoy() {
        paquete = new PaqueteTuristico("CUS-01", "Cusco", TipoDestino.NACIONAL, HOY, 4, new BigDecimal("1450.00"), 10);
        paqueteExiste(); assertThrows(ReservaException.class, this::reservar); assertEquals(10, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK05 RN4: cupos insuficientes") void cuposInsuficientes() {
        paquete = new PaqueteTuristico("CUS-01", "Cusco", TipoDestino.NACIONAL, HOY.plusDays(1), 4, new BigDecimal("1450.00"), 1);
        paqueteExiste(); assertThrows(ReservaException.class, this::reservar); assertEquals(1, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK06 RN6: pago rechazado conserva cupos") void pagoRechazado() {
        paqueteExiste(); when(calculadoraPrecio.calcular(paquete, 2, CategoriaCliente.FRECUENTE, false)).thenReturn(new BigDecimal("100.00"));
        when(pasarelaPago.cobrar("4111111111111111", new BigDecimal("100.00"))).thenReturn(ResultadoPago.rechazado("sin fondos"));
        assertTrue(assertThrows(PagoRechazadoException.class, this::reservar).getMessage().contains("sin fondos")); assertEquals(10, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK07 RN7-RN8: falla la notificación") void fallaNotificacionReserva() {
        paqueteExiste(); when(calculadoraPrecio.calcular(paquete, 2, CategoriaCliente.FRECUENTE, false)).thenReturn(new BigDecimal("100.00"));
        when(pasarelaPago.cobrar("4111111111111111", new BigDecimal("100.00"))).thenReturn(ResultadoPago.aprobado("AUT"));
        when(reservaRepository.guardar(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new IllegalStateException("correo")).when(notificador).enviarConfirmacion(any(Reserva.class));
        Reserva r = reservar(); assertEquals(EstadoReserva.CONFIRMADA, r.getEstado()); assertEquals(8, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK12 RN5 RN7: devuelve exactamente la reserva del repositorio y conserva el seguro")
    void devuelveReservaDelRepositorio() {
        paqueteExiste();
        when(calculadoraPrecio.calcular(paquete, 2, CategoriaCliente.FRECUENTE, true)).thenReturn(new BigDecimal("107.00"));
        when(pasarelaPago.cobrar("4111111111111111", new BigDecimal("107.00"))).thenReturn(ResultadoPago.aprobado("AUT"));
        Reserva guardada = new Reserva(cliente, paquete, 2, true, new BigDecimal("107.00"), HOY);
        guardada.confirmar("AUT"); guardada.setId(42L);
        when(reservaRepository.guardar(any(Reserva.class))).thenReturn(guardada);
        Reserva resultado = service.reservar(TipoDocumento.DNI, "45678912", "CUS-01", 2, true, "4111111111111111");
        assertSame(guardada, resultado); assertTrue(resultado.isSeguroCancelacion()); assertEquals(8, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK08 RN9: reserva inexistente") void reservaInexistente() { assertThrows(ReservaException.class, () -> service.cancelar(1)); }
    @Test @DisplayName("MK09 RN9: reserva no confirmada") void reservaNoConfirmada() {
        Reserva r = reservaConfirmada(); r.cancelar(); when(reservaRepository.buscarPorId(1)).thenReturn(Optional.of(r));
        assertThrows(ReservaException.class, () -> service.cancelar(1)); assertEquals(10, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK10 RN10 RN13: reembolso redondeado y liberación pese a fallo de notificación") void cancelarConReembolso() {
        Reserva r = reservaConfirmada(); when(reservaRepository.buscarPorId(1)).thenReturn(Optional.of(r));
        when(politicaCancelacion.porcentajeReembolso(r, HOY)).thenReturn(25);
        when(pasarelaPago.reembolsar("AUT123", new BigDecimal("25.25"))).thenReturn(ResultadoPago.aprobado("DEV"));
        doThrow(new IllegalStateException("correo")).when(notificador).enviarCancelacion(eq(r), any(BigDecimal.class));
        assertEquals(new BigDecimal("25.25"), service.cancelar(1)); assertEquals(EstadoReserva.CANCELADA, r.getEstado()); assertEquals(12, paquete.getCuposDisponibles());
    }
    @Test @DisplayName("MK11 RN12: reembolso rechazado") void reembolsoRechazado() {
        Reserva r = reservaConfirmada(); when(reservaRepository.buscarPorId(1)).thenReturn(Optional.of(r));
        when(politicaCancelacion.porcentajeReembolso(r, HOY)).thenReturn(50);
        when(pasarelaPago.reembolsar("AUT123", new BigDecimal("50.51"))).thenReturn(ResultadoPago.rechazado("rechazado"));
        assertThrows(PagoRechazadoException.class, () -> service.cancelar(1)); assertEquals(EstadoReserva.CONFIRMADA, r.getEstado()); assertEquals(10, paquete.getCuposDisponibles());
    }
    @Test @Tag("defecto") @DisplayName("DEF02 RN11: cero no debe consultar la pasarela") void cancelarSinReembolso() {
        Reserva r = reservaConfirmada(); when(reservaRepository.buscarPorId(1)).thenReturn(Optional.of(r));
        when(politicaCancelacion.porcentajeReembolso(r, HOY)).thenReturn(0);
        assertEquals(new BigDecimal("0.00"), service.cancelar(1)); assertEquals(EstadoReserva.CANCELADA, r.getEstado()); assertEquals(12, paquete.getCuposDisponibles());
    }
}
