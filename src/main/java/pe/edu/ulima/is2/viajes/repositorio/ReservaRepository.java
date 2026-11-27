package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.Reserva;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    /** Guarda la reserva; si no tiene id, le asigna uno. Devuelve la reserva guardada. */
    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(long id);

    List<Reserva> listar();
}
