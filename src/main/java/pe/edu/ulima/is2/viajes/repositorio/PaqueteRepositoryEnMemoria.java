package pe.edu.ulima.is2.viajes.repositorio;

import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaqueteRepositoryEnMemoria implements PaqueteRepository {

    private final Map<String, PaqueteTuristico> paquetes = new LinkedHashMap<>();

    @Override
    public void guardar(PaqueteTuristico paquete) {
        paquetes.put(paquete.getCodigo(), paquete);
    }

    @Override
    public Optional<PaqueteTuristico> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(paquetes.get(codigo));
    }

    @Override
    public List<PaqueteTuristico> listar() {
        return new ArrayList<>(paquetes.values());
    }
}
