package pe.edu.ulima.is2.viajes.integracion;

import pe.edu.ulima.is2.viajes.modelo.Reserva;

import java.math.BigDecimal;

/** Implementación de demostración: en lugar de enviar un correo, lo imprime en consola. */
public class NotificadorConsola implements Notificador {

    @Override
    public void enviarConfirmacion(Reserva reserva) {
        System.out.println("[correo a " + reserva.getCliente().getEmail() + "] Su reserva #" + reserva.getId()
                + " a " + reserva.getPaquete().getDestino() + " está confirmada.");
    }

    @Override
    public void enviarCancelacion(Reserva reserva, BigDecimal montoReembolsado) {
        System.out.println("[correo a " + reserva.getCliente().getEmail() + "] Su reserva #" + reserva.getId()
                + " fue cancelada. Reembolso: S/ " + montoReembolsado);
    }
}
