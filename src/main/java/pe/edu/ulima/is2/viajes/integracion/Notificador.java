package pe.edu.ulima.is2.viajes.integracion;

import pe.edu.ulima.is2.viajes.modelo.Reserva;

import java.math.BigDecimal;

/** Envía comunicaciones al cliente (correo, SMS, WhatsApp...). */
public interface Notificador {

    void enviarConfirmacion(Reserva reserva);

    void enviarCancelacion(Reserva reserva, BigDecimal montoReembolsado);
}
