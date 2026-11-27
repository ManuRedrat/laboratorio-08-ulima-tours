package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    void guardar(Cliente cliente);

    /** Busca por tipo y número de documento (comparación exacta). */
    Optional<Cliente> buscarPorDocumento(TipoDocumento tipo, String numero);

    List<Cliente> listar();
}
