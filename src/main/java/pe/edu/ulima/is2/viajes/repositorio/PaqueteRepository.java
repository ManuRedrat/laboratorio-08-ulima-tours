package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository {

    void guardar(PaqueteTuristico paquete);

    Optional<PaqueteTuristico> buscarPorCodigo(String codigo);

    List<PaqueteTuristico> listar();
}
