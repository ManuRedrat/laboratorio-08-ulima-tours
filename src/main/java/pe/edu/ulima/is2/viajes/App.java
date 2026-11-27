package pe.edu.ulima.is2.viajes;

import pe.edu.ulima.is2.viajes.integracion.NotificadorConsola;
import pe.edu.ulima.is2.viajes.integracion.PasarelaPagoSimulada;
import pe.edu.ulima.is2.viajes.modelo.CategoriaCliente;
import pe.edu.ulima.is2.viajes.modelo.Cliente;
import pe.edu.ulima.is2.viajes.modelo.PaqueteTuristico;
import pe.edu.ulima.is2.viajes.modelo.Reserva;
import pe.edu.ulima.is2.viajes.modelo.TipoDestino;
import pe.edu.ulima.is2.viajes.modelo.TipoDocumento;
import pe.edu.ulima.is2.viajes.reportes.GeneradorComprobante;
import pe.edu.ulima.is2.viajes.reportes.ReporteVentas;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepository;
import pe.edu.ulima.is2.viajes.repositorio.ClienteRepositoryEnMemoria;
import pe.edu.ulima.is2.viajes.repositorio.PaqueteRepository;
import pe.edu.ulima.is2.viajes.repositorio.PaqueteRepositoryEnMemoria;
import pe.edu.ulima.is2.viajes.repositorio.ReservaRepository;
import pe.edu.ulima.is2.viajes.repositorio.ReservaRepositoryEnMemoria;
import pe.edu.ulima.is2.viajes.servicio.CalculadoraPrecio;
import pe.edu.ulima.is2.viajes.servicio.PoliticaCancelacion;
import pe.edu.ulima.is2.viajes.servicio.RegistroClienteService;
import pe.edu.ulima.is2.viajes.servicio.ReservaException;
import pe.edu.ulima.is2.viajes.servicio.ReservaService;
import pe.edu.ulima.is2.viajes.servicio.ValidacionException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Scanner;

/** Aplicación de consola de la agencia de viajes "ULIMA Tours". Arma las capas y muestra un menú. */
public class App {

    private final Clock clock = Clock.systemDefaultZone();
    private final ClienteRepository clientes = new ClienteRepositoryEnMemoria();
    private final PaqueteRepository paquetes = new PaqueteRepositoryEnMemoria();
    private final ReservaRepository reservas = new ReservaRepositoryEnMemoria();
    private final CalculadoraPrecio calculadora = new CalculadoraPrecio();
    private final RegistroClienteService registro = new RegistroClienteService(clientes, clock);
    private final ReservaService reservaService = new ReservaService(clientes, paquetes, reservas, calculadora,
            new PoliticaCancelacion(), new PasarelaPagoSimulada(), new NotificadorConsola(), clock);
    private final GeneradorComprobante comprobantes = new GeneradorComprobante();
    private final ReporteVentas reporte = new ReporteVentas();
    private final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        App app = new App();
        app.cargarDatosDeEjemplo();
        app.ejecutar();
    }

    private void cargarDatosDeEjemplo() {
        LocalDate hoy = LocalDate.now(clock);
        paquetes.guardar(new PaqueteTuristico("CUS-01", "Cusco y Machu Picchu", TipoDestino.NACIONAL,
                hoy.plusDays(10), 4, new BigDecimal("1450.00"), 20));
        paquetes.guardar(new PaqueteTuristico("ARE-01", "Arequipa y Colca", TipoDestino.NACIONAL,
                hoy.plusDays(20), 3, new BigDecimal("890.00"), 15));
        paquetes.guardar(new PaqueteTuristico("CAN-01", "Cancún todo incluido", TipoDestino.INTERNACIONAL,
                hoy.plusDays(45), 7, new BigDecimal("3200.00"), 12));
        paquetes.guardar(new PaqueteTuristico("MAD-01", "Madrid y Barcelona", TipoDestino.INTERNACIONAL,
                hoy.plusMonths(3), 10, new BigDecimal("5600.00"), 8));
        clientes.guardar(new Cliente(TipoDocumento.DNI, "45678912", "Ana Torres", "ana@correo.pe",
                LocalDate.of(1990, 5, 12), CategoriaCliente.VIP));
        clientes.guardar(new Cliente(TipoDocumento.DNI, "70123456", "Luis Quispe", "luis@correo.pe",
                LocalDate.of(2001, 9, 3), CategoriaCliente.REGULAR));
    }

    private void ejecutar() {
        while (true) {
            System.out.println("""

                    ===== ULIMA TOURS =====
                    1. Listar paquetes        5. Reservar paquete
                    2. Listar clientes        6. Cancelar reserva
                    3. Registrar cliente      7. Listar reservas
                    4. Cotizar paquete        8. Emitir comprobante
                    9. Reporte de ventas del mes
                    0. Salir""");
            String opcion = leer("Opción");
            try {
                switch (opcion) {
                    case "1" -> paquetes.listar().forEach(System.out::println);
                    case "2" -> clientes.listar().forEach(System.out::println);
                    case "3" -> registrarCliente();
                    case "4" -> cotizar();
                    case "5" -> reservar();
                    case "6" -> cancelar();
                    case "7" -> reservas.listar().forEach(System.out::println);
                    case "8" -> emitirComprobante();
                    case "9" -> System.out.print(reporte.generar(reservas.listar(),
                            LocalDate.now(clock).getMonthValue(), LocalDate.now(clock).getYear()));
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Opción no válida");
                }
            } catch (ValidacionException e) {
                System.out.println("Dato inválido (" + e.getCampo() + "): " + e.getMessage());
            } catch (ReservaException | IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void registrarCliente() {
        Cliente c = registro.registrar(leer("Tipo de documento (DNI/PASAPORTE)"), leer("Número de documento"),
                leer("Nombres"), leer("Correo"), leer("Fecha de nacimiento (AAAA-MM-DD)"));
        System.out.println("Cliente registrado: " + c);
    }

    private void cotizar() {
        PaqueteTuristico paquete = paquetes.buscarPorCodigo(leer("Código de paquete"))
                .orElseThrow(() -> new ReservaException("Paquete no encontrado"));
        int pasajeros = Integer.parseInt(leer("Pasajeros"));
        CategoriaCliente categoria = CategoriaCliente.valueOf(leer("Categoría (REGULAR/FRECUENTE/VIP)").toUpperCase());
        boolean seguro = leer("¿Seguro de cancelación? (s/n)").equalsIgnoreCase("s");
        System.out.println("Total: S/ " + calculadora.calcular(paquete, pasajeros, categoria, seguro));
    }

    private void reservar() {
        TipoDocumento tipo = TipoDocumento.valueOf(leer("Tipo de documento (DNI/PASAPORTE)").toUpperCase());
        String numero = leer("Número de documento");
        String codigo = leer("Código de paquete");
        int pasajeros = Integer.parseInt(leer("Pasajeros"));
        boolean seguro = leer("¿Seguro de cancelación? (s/n)").equalsIgnoreCase("s");
        String tarjeta = leer("Número de tarjeta (16 dígitos)");
        System.out.println("Procesando pago...");
        Reserva r = reservaService.reservar(tipo, numero, codigo, pasajeros, seguro, tarjeta);
        System.out.println("Reserva confirmada: " + r);
    }

    private void cancelar() {
        long id = Long.parseLong(leer("Id de reserva"));
        System.out.println("Procesando...");
        BigDecimal reembolso = reservaService.cancelar(id);
        System.out.println("Reserva cancelada. Reembolso: S/ " + reembolso);
    }

    private void emitirComprobante() {
        Reserva r = reservas.buscarPorId(Long.parseLong(leer("Id de reserva")))
                .orElseThrow(() -> new ReservaException("Reserva no encontrada"));
        String tipo = leer("Tipo (BOLETA/FACTURA)").toUpperCase();
        String ruc = null;
        String razonSocial = null;
        String direccion = null;
        if (tipo.equals("FACTURA")) {
            ruc = leer("RUC");
            razonSocial = leer("Razón social");
            direccion = leer("Dirección fiscal (opcional)");
        }
        System.out.print(comprobantes.generar(r, tipo, ruc, razonSocial, direccion, true, LocalDate.now(clock)));
    }

    private String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }
}
