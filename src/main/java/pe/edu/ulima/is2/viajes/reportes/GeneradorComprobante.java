package pe.edu.ulima.is2.viajes.reportes;

import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.EstadoReserva;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Genera el texto del comprobante de pago (boleta o factura) de una reserva.
 */
public class GeneradorComprobante {

    private int c = 1;  // correlativo de boletas
    private int c2 = 1; // correlativo de facturas

    // Genera el comprobante
    public String generar(Reserva r, String tipo, String ruc, String razonSocial, String direccion,
                          boolean detalle, LocalDate fecha) {
        String s = "";
        // obtener el total
        double t = r.getMontoTotal().doubleValue();
        if (r.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalStateException("No se emite comprobante de una reserva cancelada");
        }
        if (tipo.equals("BOLETA")) {
            // cabecera
            s += "========================================\n";
            s += "       AGENCIA DE VIAJES ULIMA TOURS\n";
            s += "            RUC 20123456789\n";
            s += "========================================\n";
            s += "BOLETA DE VENTA ELECTRONICA B001-" + String.format("%06d", c) + "\n";
            c++;
            s += "Fecha de emision: " + (fecha.getDayOfMonth() < 10 ? "0" : "") + fecha.getDayOfMonth() + "/"
                    + (fecha.getMonthValue() < 10 ? "0" : "") + fecha.getMonthValue() + "/" + fecha.getYear() + "\n";
            s += "Cliente: " + r.getCliente().getNombres() + "\n";
            // si el monto supera 700 se debe mostrar el documento del cliente
            if (t > 700) {
                s += "Documento: " + r.getCliente().getTipoDocumento() + " "
                        + r.getCliente().getNumeroDocumento() + "\n";
            }
            s += "----------------------------------------\n";
            if (detalle) {
                // detalle del paquete
                s += "Paquete: " + r.getPaquete().getCodigo() + " - " + r.getPaquete().getDestino() + "\n";
                s += "Salida: " + (r.getPaquete().getFechaSalida().getDayOfMonth() < 10 ? "0" : "")
                        + r.getPaquete().getFechaSalida().getDayOfMonth() + "/"
                        + (r.getPaquete().getFechaSalida().getMonthValue() < 10 ? "0" : "")
                        + r.getPaquete().getFechaSalida().getMonthValue() + "/"
                        + r.getPaquete().getFechaSalida().getYear() + " (" + r.getPaquete().getDuracionDias()
                        + " dias)\n";
                s += "Pasajeros: " + r.getPasajeros() + " x S/ "
                        + String.format(Locale.US, "%.2f", r.getPaquete().getPrecioPorPersona().doubleValue()) + "\n";
                if (r.isSeguroCancelacion()) {
                    s += "Incluye seguro de cancelacion\n";
                }
                s += "----------------------------------------\n";
            }
            s += "TOTAL: S/ " + String.format(Locale.US, "%.2f", t) + "\n";
        } else if (tipo.equals("FACTURA")) {
            // validar ruc
            if (ruc == null || ruc.length() != 11 || !(ruc.startsWith("10") || ruc.startsWith("20"))) {
                throw new IllegalArgumentException("RUC invalido");
            }
            if (razonSocial == null || razonSocial.trim().isEmpty()) {
                throw new IllegalArgumentException("La razon social es obligatoria");
            }
            // cabecera
            s += "========================================\n";
            s += "       AGENCIA DE VIAJES ULIMA TOURS\n";
            s += "            RUC 20123456789\n";
            s += "========================================\n";
            s += "FACTURA ELECTRONICA F001-" + String.format("%06d", c2) + "\n";
            c2++;
            s += "Fecha de emision: " + (fecha.getDayOfMonth() < 10 ? "0" : "") + fecha.getDayOfMonth() + "/"
                    + (fecha.getMonthValue() < 10 ? "0" : "") + fecha.getMonthValue() + "/" + fecha.getYear() + "\n";
            s += "Senor(es): " + razonSocial.trim() + "\n";
            s += "RUC: " + ruc + "\n";
            if (direccion != null && !direccion.trim().isEmpty()) {
                s += "Direccion: " + direccion.trim() + "\n";
            }
            s += "----------------------------------------\n";
            if (detalle) {
                // detalle del paquete
                s += "Paquete: " + r.getPaquete().getCodigo() + " - " + r.getPaquete().getDestino() + "\n";
                s += "Salida: " + (r.getPaquete().getFechaSalida().getDayOfMonth() < 10 ? "0" : "")
                        + r.getPaquete().getFechaSalida().getDayOfMonth() + "/"
                        + (r.getPaquete().getFechaSalida().getMonthValue() < 10 ? "0" : "")
                        + r.getPaquete().getFechaSalida().getMonthValue() + "/"
                        + r.getPaquete().getFechaSalida().getYear() + " (" + r.getPaquete().getDuracionDias()
                        + " dias)\n";
                s += "Pasajeros: " + r.getPasajeros() + " x S/ "
                        + String.format(Locale.US, "%.2f", r.getPaquete().getPrecioPorPersona().doubleValue()) + "\n";
                if (r.isSeguroCancelacion()) {
                    s += "Incluye seguro de cancelacion\n";
                }
                s += "----------------------------------------\n";
            }
            // los paquetes internacionales son exportacion de servicios y no pagan IGV
            if (r.getPaquete().getTipoDestino() == TipoDestino.INTERNACIONAL) {
                s += "OP. EXONERADA: S/ " + String.format(Locale.US, "%.2f", t) + "\n";
                s += "IGV (18%): S/ " + String.format(Locale.US, "%.2f", 0.0) + "\n";
            } else {
                double b = t / 1.18;
                double i = t - b;
                s += "OP. GRAVADA: S/ " + String.format(Locale.US, "%.2f", b) + "\n";
                s += "IGV (18%): S/ " + String.format(Locale.US, "%.2f", i) + "\n";
            }
            s += "TOTAL: S/ " + String.format(Locale.US, "%.2f", t) + "\n";
        } else {
            throw new IllegalArgumentException("Tipo de comprobante desconocido: " + tipo);
        }
        // pie
        s += "========================================\n";
        if (r.getCliente().getCategoria() == CategoriaCliente.VIP) {
            s += "Gracias por ser cliente VIP!\n";
        } else if (r.getCliente().getCategoria() == CategoriaCliente.FRECUENTE) {
            s += "Gracias por viajar nuevamente con nosotros\n";
        } else {
            s += "Gracias por su preferencia\n";
        }
        return s;
    }

    // formato antiguo de comprobante (ya no se usa, pero lo dejamos por si acaso)
    private String formatoAntiguo(Reserva r) {
        String s = "COMPROBANTE\n";
        s += r.getCliente().getNombres() + "\n";
        s += "S/ " + r.getMontoTotal() + "\n";
        return s;
    }
}
