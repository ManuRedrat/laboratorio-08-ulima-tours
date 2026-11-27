package pe.edu.ulima.is2.viajes.servicio;

import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Caso de uso "Registrar cliente". La especificación está en ENUNCIADO.md (Parte B). */
public class RegistroClienteService {

    static final int NOMBRES_MIN = 2;
    static final int NOMBRES_MAX = 60;
    static final int EDAD_MINIMA = 18;

    private static final Pattern DNI = Pattern.compile("\\d{8}");
    private static final Pattern PASAPORTE = Pattern.compile("[A-Za-z0-9]{6,12}");
    private static final Pattern NOMBRES = Pattern.compile("[\\p{L} ]+");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+");

    private final ClienteRepository clienteRepository;
    private final Clock clock;

    public RegistroClienteService(ClienteRepository clienteRepository, Clock clock) {
        this.clienteRepository = clienteRepository;
        this.clock = clock;
    }

    public Cliente registrar(String tipoDocumento, String numeroDocumento, String nombres,
                             String email, String fechaNacimiento) {
        TipoDocumento tipo = validarTipoDocumento(tipoDocumento);
        String numero = validarNumeroDocumento(tipo, numeroDocumento);
        String nombresLimpios = validarNombres(nombres);
        String emailLimpio = validarEmail(email);
        LocalDate nacimiento = validarFechaNacimiento(fechaNacimiento);

        if (clienteRepository.buscarPorDocumento(tipo, numero).isPresent()) {
            throw new ValidacionException("numeroDocumento", "Ya existe un cliente con ese documento");
        }
        if (tipo == TipoDocumento.PASAPORTE) {
            numero = numero.toUpperCase(Locale.ROOT);
        }

        Cliente cliente = new Cliente(tipo, numero, nombresLimpios, emailLimpio, nacimiento,
                CategoriaCliente.REGULAR);
        clienteRepository.guardar(cliente);
        return cliente;
    }

    private TipoDocumento validarTipoDocumento(String tipoDocumento) {
        if (estaVacio(tipoDocumento)) {
            throw new ValidacionException("tipoDocumento", "El tipo de documento es obligatorio");
        }
        try {
            return TipoDocumento.valueOf(tipoDocumento.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("tipoDocumento", "Tipo de documento no soportado: " + tipoDocumento);
        }
    }

    private String validarNumeroDocumento(TipoDocumento tipo, String numeroDocumento) {
        if (estaVacio(numeroDocumento)) {
            throw new ValidacionException("numeroDocumento", "El número de documento es obligatorio");
        }
        String numero = numeroDocumento.trim();
        Pattern formato = tipo == TipoDocumento.DNI ? DNI : PASAPORTE;
        if (!formato.matcher(numero).matches()) {
            throw new ValidacionException("numeroDocumento", "Formato de " + tipo + " inválido");
        }
        return numero;
    }

    private String validarNombres(String nombres) {
        if (estaVacio(nombres)) {
            throw new ValidacionException("nombres", "Los nombres son obligatorios");
        }
        String limpio = nombres.trim();
        if (limpio.length() < NOMBRES_MIN || limpio.length() > NOMBRES_MAX) {
            throw new ValidacionException("nombres",
                    "Los nombres deben tener entre " + NOMBRES_MIN + " y " + NOMBRES_MAX + " caracteres");
        }
        if (!NOMBRES.matcher(limpio).matches()) {
            throw new ValidacionException("nombres", "Los nombres solo pueden contener letras y espacios");
        }
        return limpio;
    }

    private String validarEmail(String email) {
        if (estaVacio(email)) {
            throw new ValidacionException("email", "El correo es obligatorio");
        }
        String limpio = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(limpio).matches()) {
            throw new ValidacionException("email", "Formato de correo inválido");
        }
        return limpio;
    }

    private LocalDate validarFechaNacimiento(String fechaNacimiento) {
        if (estaVacio(fechaNacimiento)) {
            throw new ValidacionException("fechaNacimiento", "La fecha de nacimiento es obligatoria");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaNacimiento.trim());
        } catch (DateTimeParseException e) {
            throw new ValidacionException("fechaNacimiento", "Use el formato AAAA-MM-DD con una fecha válida");
        }
        LocalDate hoy = LocalDate.now(clock);
        if (Period.between(fecha, hoy).getYears() < EDAD_MINIMA) {
            throw new ValidacionException("fechaNacimiento", "El titular debe ser mayor de edad");
        }
        return fecha;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
