package pe.edu.ulima.is2.viajes.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Paquete turístico ofrecido por la agencia: destino, salida, precio por persona y cupos. */
public class PaqueteTuristico {

    private final String codigo;
    private final String destino;
    private final TipoDestino tipoDestino;
    private final LocalDate fechaSalida;
    private final int duracionDias;
    private final BigDecimal precioPorPersona;
    private int cuposDisponibles;

    public PaqueteTuristico(String codigo, String destino, TipoDestino tipoDestino, LocalDate fechaSalida,
                            int duracionDias, BigDecimal precioPorPersona, int cuposDisponibles) {
        if (cuposDisponibles < 0) {
            throw new IllegalArgumentException("Los cupos no pueden ser negativos");
        }
        this.codigo = Objects.requireNonNull(codigo);
        this.destino = Objects.requireNonNull(destino);
        this.tipoDestino = Objects.requireNonNull(tipoDestino);
        this.fechaSalida = Objects.requireNonNull(fechaSalida);
        this.duracionDias = duracionDias;
        this.precioPorPersona = Objects.requireNonNull(precioPorPersona);
        this.cuposDisponibles = cuposDisponibles;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDestino() {
        return destino;
    }

    public TipoDestino getTipoDestino() {
        return tipoDestino;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public BigDecimal getPrecioPorPersona() {
        return precioPorPersona;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public boolean tieneCupos(int cantidad) {
        return cuposDisponibles >= cantidad;
    }

    public void reservarCupos(int cantidad) {
        if (!tieneCupos(cantidad)) {
            throw new IllegalStateException("No hay cupos suficientes en " + codigo);
        }
        cuposDisponibles -= cantidad;
    }

    public void liberarCupos(int cantidad) {
        cuposDisponibles += cantidad;
    }

    @Override
    public String toString() {
        return codigo + " - " + destino + " (" + tipoDestino + "), sale " + fechaSalida
                + ", " + duracionDias + " días, S/ " + precioPorPersona + " p.p., cupos: " + cuposDisponibles;
    }
}
