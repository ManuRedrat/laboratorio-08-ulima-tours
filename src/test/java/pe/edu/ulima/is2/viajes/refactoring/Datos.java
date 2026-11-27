package pe.edu.ulima.is2.viajes.refactoring;

import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de prueba compartidos por las pruebas de caracterización. */
final class Datos {

    static final Cliente ANA_VIP = new Cliente(TipoDocumento.DNI, "45678912", "Ana Torres", "ana@correo.pe",
            LocalDate.of(1990, 5, 12), CategoriaCliente.VIP);
    static final Cliente JOHN_REGULAR = new Cliente(TipoDocumento.PASAPORTE, "AB123456", "John Smith",
            "john@mail.com", LocalDate.of(1985, 1, 2), CategoriaCliente.REGULAR);
    static final Cliente LUIS_FRECUENTE = new Cliente(TipoDocumento.DNI, "70123456", "Luis Quispe",
            "luis@correo.pe", LocalDate.of(2001, 9, 3), CategoriaCliente.FRECUENTE);

    private Datos() {
    }

    static PaqueteTuristico cusco() {
        return new PaqueteTuristico("CUS-01", "Cusco y Machu Picchu", TipoDestino.NACIONAL,
                LocalDate.of(2027, 3, 5), 4, new BigDecimal("1450.00"), 20);
    }

    static PaqueteTuristico paracas() {
        return new PaqueteTuristico("PAR-01", "Paracas", TipoDestino.NACIONAL,
                LocalDate.of(2027, 1, 9), 2, new BigDecimal("320.00"), 20);
    }

    static PaqueteTuristico cancun() {
        return new PaqueteTuristico("CAN-01", "Cancun todo incluido", TipoDestino.INTERNACIONAL,
                LocalDate.of(2027, 12, 20), 7, new BigDecimal("3200.00"), 12);
    }

    static Reserva confirmada(Cliente cliente, PaqueteTuristico paquete, int pasajeros, boolean seguro,
                              String monto, LocalDate fecha) {
        Reserva reserva = new Reserva(cliente, paquete, pasajeros, seguro, new BigDecimal(monto), fecha);
        reserva.confirmar("AUT-" + pasajeros);
        return reserva;
    }
}
