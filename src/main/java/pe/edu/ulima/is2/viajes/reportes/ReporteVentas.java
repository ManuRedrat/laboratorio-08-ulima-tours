package pe.edu.ulima.is2.viajes.reportes;

import pe.edu.ulima.is2.viajes.modelo.EstadoReserva;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reporte mensual de ventas de la agencia.
 */
public class ReporteVentas {

    public String generar(List<Reserva> lista, int mes, int anio) {
        String rep = "REPORTE DE VENTAS " + (mes < 10 ? "0" : "") + mes + "/" + anio + "\n";
        rep += "----------------------------------------\n";

        // ventas nacionales
        double tn = 0;
        int cn = 0;
        int pn = 0;
        for (Reserva x : lista) {
            if (x.getEstado() == EstadoReserva.CONFIRMADA && x.getFechaReserva().getMonthValue() == mes
                    && x.getFechaReserva().getYear() == anio) {
                if (x.getPaquete().getTipoDestino() == TipoDestino.NACIONAL) {
                    tn = tn + x.getMontoTotal().doubleValue();
                    cn++;
                    pn = pn + x.getPasajeros();
                }
            }
        }
        rep += "Nacional:      " + cn + " reservas, " + pn + " pasajeros, S/ "
                + String.format(Locale.US, "%.2f", tn) + "\n";

        // ventas internacionales
        double ti = 0;
        int ci = 0;
        int pi = 0;
        for (Reserva x : lista) {
            if (x.getEstado() == EstadoReserva.CONFIRMADA && x.getFechaReserva().getMonthValue() == mes
                    && x.getFechaReserva().getYear() == anio) {
                if (x.getPaquete().getTipoDestino() == TipoDestino.INTERNACIONAL) {
                    ti = ti + x.getMontoTotal().doubleValue();
                    ci++;
                    pi = pi + x.getPasajeros();
                }
            }
        }
        rep += "Internacional: " + ci + " reservas, " + pi + " pasajeros, S/ "
                + String.format(Locale.US, "%.2f", ti) + "\n";
        rep += "TOTAL:         " + (cn + ci) + " reservas, " + (pn + pi) + " pasajeros, S/ "
                + String.format(Locale.US, "%.2f", tn + ti) + "\n";

        // comision para los vendedores segun el total vendido
        double com;
        if (tn + ti > 50000) {
            com = (tn + ti) * 0.05;
        } else if (tn + ti > 20000) {
            com = (tn + ti) * 0.03;
        } else {
            com = (tn + ti) * 0.01;
        }
        rep += "Comision vendedores: S/ " + String.format(Locale.US, "%.2f", com) + "\n";

        // destino mas vendido (por pasajeros)
        Map<String, Integer> m = new TreeMap<>();
        for (Reserva x : lista) {
            if (x.getEstado() == EstadoReserva.CONFIRMADA && x.getFechaReserva().getMonthValue() == mes
                    && x.getFechaReserva().getYear() == anio) {
                String d = x.getPaquete().getDestino();
                if (m.containsKey(d)) {
                    m.put(d, m.get(d) + x.getPasajeros());
                } else {
                    m.put(d, x.getPasajeros());
                }
            }
        }
        String top = "-";
        int max = 0;
        for (String k : m.keySet()) {
            if (m.get(k) > max) {
                max = m.get(k);
                top = k;
            }
        }
        rep += "Destino mas vendido: " + top + (max > 0 ? " (" + max + " pasajeros)" : "") + "\n";
        return rep;
    }
}
