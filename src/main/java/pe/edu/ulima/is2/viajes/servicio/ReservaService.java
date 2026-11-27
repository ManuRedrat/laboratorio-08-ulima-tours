package pe.edu.ulima.is2.viajes.servicio;

import pe.edu.ulima.is2.viajes.integracion.Notificador;
import pe.edu.ulima.is2.viajes.integracion.PasarelaPago;
import pe.edu.ulima.is2.viajes.integracion.ResultadoPago;
import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.EstadoReserva;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepository;
import pe.edu.ulima.is2.viajes.repositorio.PaqueteRepository;
import pe.edu.ulima.is2.viajes.repositorio.ReservaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Casos de uso "Reservar paquete" y "Cancelar reserva". Reglas en ENUNCIADO.md (Parte C). */
public class ReservaService {

    private static final Logger LOG = Logger.getLogger(ReservaService.class.getName());

    private final ClienteRepository clienteRepository;
    private final PaqueteRepository paqueteRepository;
    private final ReservaRepository reservaRepository;
    private final CalculadoraPrecio calculadoraPrecio;
    private final PoliticaCancelacion politicaCancelacion;
    private final PasarelaPago pasarelaPago;
    private final Notificador notificador;
    private final Clock clock;

    public ReservaService(ClienteRepository clienteRepository, PaqueteRepository paqueteRepository,
                          ReservaRepository reservaRepository, CalculadoraPrecio calculadoraPrecio,
                          PoliticaCancelacion politicaCancelacion, PasarelaPago pasarelaPago,
                          Notificador notificador, Clock clock) {
        this.clienteRepository = clienteRepository;
        this.paqueteRepository = paqueteRepository;
        this.reservaRepository = reservaRepository;
        this.calculadoraPrecio = calculadoraPrecio;
        this.politicaCancelacion = politicaCancelacion;
        this.pasarelaPago = pasarelaPago;
        this.notificador = notificador;
        this.clock = clock;
    }

    public Reserva reservar(TipoDocumento tipoDocumento, String numeroDocumento, String codigoPaquete,
                            int pasajeros, boolean conSeguro, String numeroTarjeta) {
        Cliente cliente = clienteRepository.buscarPorDocumento(tipoDocumento, numeroDocumento)
                .orElseThrow(() -> new ReservaException("Cliente no registrado"));
        PaqueteTuristico paquete = paqueteRepository.buscarPorCodigo(codigoPaquete)
                .orElseThrow(() -> new ReservaException("Paquete no encontrado: " + codigoPaquete));

        LocalDate hoy = LocalDate.now(clock);
        if (!paquete.getFechaSalida().isAfter(hoy)) {
            throw new ReservaException("El paquete " + codigoPaquete + " ya no admite reservas");
        }
        if (!paquete.tieneCupos(pasajeros)) {
            throw new ReservaException("No hay cupos suficientes en " + codigoPaquete);
        }

        BigDecimal monto = calculadoraPrecio.calcular(paquete, pasajeros, cliente.getCategoria(), conSeguro);
        ResultadoPago pago = pasarelaPago.cobrar(numeroTarjeta, monto);
        if (!pago.aprobado()) {
            throw new PagoRechazadoException("Pago rechazado: " + pago.mensaje());
        }

        paquete.reservarCupos(pasajeros);
        paqueteRepository.guardar(paquete);

        Reserva reserva = new Reserva(cliente, paquete, pasajeros, conSeguro, monto, hoy);
        reserva.confirmar(pago.codigoAutorizacion());
        reserva = reservaRepository.guardar(reserva);

        try {
            notificador.enviarConfirmacion(reserva);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo notificar la reserva " + reserva.getId(), e);
        }
        return reserva;
    }

    /** @return monto reembolsado al cliente */
    public BigDecimal cancelar(long idReserva) {
        Reserva reserva = reservaRepository.buscarPorId(idReserva)
                .orElseThrow(() -> new ReservaException("Reserva no encontrada: " + idReserva));
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReservaException("Solo se pueden cancelar reservas confirmadas");
        }

        int porcentaje = politicaCancelacion.porcentajeReembolso(reserva, LocalDate.now(clock));
        BigDecimal reembolso = reserva.getMontoTotal()
                .multiply(BigDecimal.valueOf(porcentaje))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        if (porcentaje >= 0) {
            ResultadoPago resultado = pasarelaPago.reembolsar(reserva.getCodigoAutorizacion(), reembolso);
            if (!resultado.aprobado()) {
                throw new PagoRechazadoException("No se pudo procesar el reembolso: " + resultado.mensaje());
            }
        }

        reserva.cancelar();
        PaqueteTuristico paquete = reserva.getPaquete();
        paquete.liberarCupos(reserva.getPasajeros());
        paqueteRepository.guardar(paquete);
        reservaRepository.guardar(reserva);

        try {
            notificador.enviarCancelacion(reserva, reembolso);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo notificar la cancelación de la reserva " + reserva.getId(), e);
        }
        return reembolso;
    }
}
