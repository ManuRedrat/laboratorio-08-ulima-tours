package pe.edu.ulima.is2.viajes.servicio;

import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.EstadoReserva;
import pe.edu.ulima.is2.viajes.modelo.Reserva;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Determina qué porcentaje del monto pagado se devuelve al cancelar una reserva. */
public class PoliticaCancelacion {

    static final int DIAS_ANTICIPACION_ALTA = 30;
    static final int DIAS_ANTICIPACION_MEDIA = 15;
    static final int DIAS_ANTICIPACION_MINIMA = 7;

    /**
     * @param reserva          reserva que se desea cancelar
     * @param fechaCancelacion día en que el cliente solicita la cancelación
     * @return porcentaje a reembolsar (0 a 100)
     */
    public int porcentajeReembolso(Reserva reserva, LocalDate fechaCancelacion) {
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            return 0;
        }
        long diasAntes = ChronoUnit.DAYS.between(fechaCancelacion, reserva.getPaquete().getFechaSalida());
        if (diasAntes < 0) {
            return 0;
        }
        if (reserva.isSeguroCancelacion()) {
            return 100;
        }
        CategoriaCliente categoria = reserva.getCliente().getCategoria();
        if (diasAntes >= DIAS_ANTICIPACION_ALTA) {
            return 90;
        }
        if (diasAntes >= DIAS_ANTICIPACION_MEDIA) {
            if (categoria == CategoriaCliente.VIP) {
                return 75;
            }
            return 50;
        }
        if (diasAntes >= DIAS_ANTICIPACION_MINIMA && categoria != CategoriaCliente.REGULAR) {
            return 25;
        }
        return 0;
    }
}
