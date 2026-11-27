package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteRepositoryEnMemoria implements ClienteRepository {

    private final List<Cliente> clientes = new ArrayList<>();

    @Override
    public void guardar(Cliente cliente) {
        clientes.removeIf(c -> c.getTipoDocumento() == cliente.getTipoDocumento()
                && c.getNumeroDocumento().equals(cliente.getNumeroDocumento()));
        clientes.add(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(TipoDocumento tipo, String numero) {
        return clientes.stream()
                .filter(c -> c.getTipoDocumento() == tipo && c.getNumeroDocumento().equals(numero))
                .findFirst();
    }

    @Override
    public List<Cliente> listar() {
        return List.copyOf(clientes);
    }
}
