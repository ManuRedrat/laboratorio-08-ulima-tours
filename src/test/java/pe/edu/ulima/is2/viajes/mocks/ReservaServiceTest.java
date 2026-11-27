package pe.edu.ulima.is2.viajes.mocks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    // TODO: implemente aquí los casos MK02, MK03, ... diseñados en la Parte C.
}
