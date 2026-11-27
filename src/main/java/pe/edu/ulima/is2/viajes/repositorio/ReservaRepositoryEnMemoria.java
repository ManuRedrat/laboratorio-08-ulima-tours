package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.Reserva;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReservaRepositoryEnMemoria implements ReservaRepository {

    private final Map<Long, Reserva> reservas = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public Reserva guardar(Reserva reserva) {
        if (reserva.getId() == null) {
            reserva.setId(siguienteId++);
        }
        reservas.put(reserva.getId(), reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(long id) {
        return Optional.ofNullable(reservas.get(id));
    }

    @Override
    public List<Reserva> listar() {
        return new ArrayList<>(reservas.values());
    }
}
