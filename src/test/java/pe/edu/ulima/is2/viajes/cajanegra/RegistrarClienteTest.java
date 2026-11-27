package pe.edu.ulima.is2.viajes.cajanegra;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import pe.edu.ulima.is2.viajes.modelo.*;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepositoryEnMemoria;
import pe.edu.ulima.is2.viajes.servicio.*;
import java.time.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class RegistrarClienteTest {
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-27T12:00:00Z"), ZoneOffset.UTC);
    private ClienteRepositoryEnMemoria repositorio;
    private RegistroClienteService servicio;
    @BeforeEach void iniciar() { repositorio = new ClienteRepositoryEnMemoria(); servicio = new RegistroClienteService(repositorio, RELOJ); }

    @Test @DisplayName("CN01: DNI, normalización y mayoría de edad exacta")
    void dniValido() {
        Cliente c = servicio.registrar("dni", " 12345678 ", "  Ana Ñúñez  ", "ANA@CORREO.PE", "2008-11-27");
        assertEquals("12345678", c.getNumeroDocumento()); assertEquals("Ana Ñúñez", c.getNombres());
        assertEquals("ana@correo.pe", c.getEmail()); assertEquals(CategoriaCliente.REGULAR, c.getCategoria());
        assertEquals(1, repositorio.listar().size());
    }
    @ParameterizedTest(name="CN02: pasaporte {0}, nombre de {1} caracteres")
    @MethodSource("validos")
    void pasaporteValido(String numero, String nombre) {
        Cliente c = servicio.registrar("pasaporte", numero, nombre, "a@b.pe", "2000-02-29");
        assertEquals(numero.toUpperCase(), c.getNumeroDocumento()); assertEquals(nombre, c.getNombres());
        assertEquals(1, repositorio.listar().size());
    }
    static Stream<Arguments> validos() { return Stream.of(Arguments.of("ab1234", "Al"), Arguments.of("ab1234567890", "A".repeat(60))); }

    @ParameterizedTest(name="{0}: campo {6}") @MethodSource("invalidos")
    void datoInvalido(String id, String tipo, String numero, String nombre, String email, String fecha, String campo) {
        ValidacionException e = assertThrows(ValidacionException.class, () -> servicio.registrar(tipo, numero, nombre, email, fecha));
        assertEquals(campo, e.getCampo(), id); assertTrue(repositorio.listar().isEmpty(), id);
    }
    static Stream<Arguments> invalidos() {
        return Stream.of(
            Arguments.of("CN03", null,"12345678","Ana","a@b.pe","2000-01-01","tipoDocumento"),
            Arguments.of("CN04", "RUC","12345678","Ana","a@b.pe","2000-01-01","tipoDocumento"),
            Arguments.of("CN05", "DNI",null,"Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN06", "DNI","1234567","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN07", "DNI","123456789","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN08", "DNI","1234567A","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN09", "PASAPORTE","A1234","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN10", "PASAPORTE","A123456789012","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN11", "PASAPORTE","ABC-123","Ana","a@b.pe","2000-01-01","numeroDocumento"),
            Arguments.of("CN12", "DNI","12345678",null,"a@b.pe","2000-01-01","nombres"),
            Arguments.of("CN13", "DNI","12345678","A","a@b.pe","2000-01-01","nombres"),
            Arguments.of("CN14", "DNI","12345678","A".repeat(61),"a@b.pe","2000-01-01","nombres"),
            Arguments.of("CN15", "DNI","12345678","Ana1","a@b.pe","2000-01-01","nombres"),
            Arguments.of("CN16", "DNI","12345678","Ana",null,"2000-01-01","email"),
            Arguments.of("CN17", "DNI","12345678","Ana","ana@correo","2000-01-01","email"),
            Arguments.of("CN18", "DNI","12345678","Ana","a@b.pe",null,"fechaNacimiento"),
            Arguments.of("CN19", "DNI","12345678","Ana","a@b.pe","2000-02-30","fechaNacimiento"),
            Arguments.of("CN20", "DNI","12345678","Ana","a@b.pe","27/11/2000","fechaNacimiento"),
            Arguments.of("CN21", "DNI","12345678","Ana","a@b.pe","2008-11-28","fechaNacimiento")
        );
    }
    @Test @DisplayName("CN22: documento duplicado") void duplicado() {
        servicio.registrar("DNI","12345678","Ana","a@b.pe","2000-01-01");
        assertEquals("numeroDocumento", assertThrows(ValidacionException.class, () -> servicio.registrar("DNI","12345678","Ana","a@b.pe","2000-01-01")).getCampo());
        assertEquals(1, repositorio.listar().size());
    }
    @Test @Tag("defecto") @DisplayName("DEF01: duplicado de pasaporte con distinta capitalización")
    void duplicadoPasaporteSinDistinguirMayusculas() {
        servicio.registrar("PASAPORTE","ABC123","Ana","a@b.pe","2000-01-01");
        assertEquals("numeroDocumento", assertThrows(ValidacionException.class, () -> servicio.registrar("PASAPORTE","abc123","Ana","a@b.pe","2000-01-01")).getCampo());
        assertEquals(1, repositorio.listar().size());
    }
}
