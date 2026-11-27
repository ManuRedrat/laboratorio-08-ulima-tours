package pe.edu.ulima.is2.viajes.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Reserva de un paquete turístico hecha por un cliente para uno o más pasajeros. */
public class Reserva {

    private Long id;
    private final Cliente cliente;
    private final PaqueteTuristico paquete;
    private final int pasajeros;
    private final boolean seguroCancelacion;
    private final BigDecimal montoTotal;
    private final LocalDate fechaReserva;
    private EstadoReserva estado;
    private String codigoAutorizacion;

    public Reserva(Cliente cliente, PaqueteTuristico paquete, int pasajeros, boolean seguroCancelacion,
                   BigDecimal montoTotal, LocalDate fechaReserva) {
        this.cliente = Objects.requireNonNull(cliente);
        this.paquete = Objects.requireNonNull(paquete);
        this.pasajeros = pasajeros;
        this.seguroCancelacion = seguroCancelacion;
        this.montoTotal = Objects.requireNonNull(montoTotal);
        this.fechaReserva = Objects.requireNonNull(fechaReserva);
        this.estado = EstadoReserva.PENDIENTE;
    }

    public void confirmar(String codigoAutorizacion) {
        this.codigoAutorizacion = Objects.requireNonNull(codigoAutorizacion);
        this.estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public PaqueteTuristico getPaquete() {
        return paquete;
    }

    public int getPasajeros() {
        return pasajeros;
    }

    public boolean isSeguroCancelacion() {
        return seguroCancelacion;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    @Override
    public String toString() {
        return "#" + id + " " + paquete.getCodigo() + " - " + cliente.getNombres() + " - " + pasajeros
                + " pax - S/ " + montoTotal + " - " + estado;
    }
}
