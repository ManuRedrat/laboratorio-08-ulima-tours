package pe.edu.ulima.is2.viajes.servicio;

/** Error de negocio al reservar o cancelar (cliente/paquete inexistente, sin cupos, etc.). */
public class ReservaException extends RuntimeException {

    public ReservaException(String mensaje) {
        super(mensaje);
    }
}
