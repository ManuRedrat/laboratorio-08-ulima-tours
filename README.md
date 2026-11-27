# ULIMA Tours — Gestión de una agencia de viajes

Aplicación de consola para gestionar paquetes turísticos, clientes, reservas, comprobantes y
reportes de ventas, usada como sistema bajo prueba en el curso de Ingeniería de Software II.

- **Lenguaje / build:** Java 17+ con Maven
- **Interfaz:** consola (menú de texto)
- **Persistencia:** repositorios en memoria
- **Pruebas:** JUnit 5, Mockito, JaCoCo (cobertura) y PIT (pruebas de mutación)

El enunciado del laboratorio está en [`ENUNCIADO.md`](ENUNCIADO.md).

## Cómo ejecutar

```bash
# Compilar, ejecutar pruebas y generar el reporte de cobertura
mvn test
# Reporte: target/site/jacoco/index.html

# Pruebas de mutación (requiere que mvn test esté en verde)
mvn test-compile org.pitest:pitest-maven:mutationCoverage
# Reporte: target/pit-reports/index.html

# PIT sobre una sola clase o paquete
mvn test-compile org.pitest:pitest-maven:mutationCoverage -Dpit.clases=pe.edu.ulima.is2.viajes.servicio.CalculadoraPrecio

# Ejecutar solo las pruebas marcadas con @Tag("defecto")
mvn test -Dexcluir.grupos=ninguno -Dgroups=defecto

# Ejecutar la aplicación
mvn -q compile exec:java
```

La aplicación carga paquetes y clientes de ejemplo al iniciar (por ejemplo, el cliente VIP
`DNI 45678912`). Para reservar se pide un número de tarjeta de 16 dígitos; la pasarela de pagos es
simulada: demora unos segundos y rechaza al azar algunas operaciones.

## Estructura

```
src/main/java/pe/edu/ulima/is2/viajes
├── App.java                        Arma las capas y muestra el menú de consola
├── modelo/                         Entidades del dominio
│   ├── Cliente, PaqueteTuristico, Reserva
│   └── TipoDocumento, CategoriaCliente, TipoDestino, EstadoReserva
├── repositorio/                    Persistencia (interfaces + implementaciones en memoria)
│   ├── ClienteRepository, PaqueteRepository, ReservaRepository
│   └── *RepositoryEnMemoria
├── integracion/                    Servicios externos
│   ├── PasarelaPago, ResultadoPago, PasarelaPagoSimulada
│   └── Notificador, NotificadorConsola
├── servicio/                       Reglas de negocio y casos de uso
│   ├── PoliticaCancelacion         % de reembolso al cancelar           (Parte A: caja blanca)
│   ├── RegistroClienteService      Registrar cliente con validaciones   (Parte B: caja negra)
│   ├── ReservaService              Reservar y cancelar                  (Parte C: mocks)
│   ├── CalculadoraPrecio           Precio de una reserva                (Parte D: mutación)
│   └── ValidacionException, ReservaException, PagoRechazadoException
└── reportes/                       Comprobantes y reporte de ventas     (Parte E: refactoring)
    ├── GeneradorComprobante
    └── ReporteVentas

src/test/java/pe/edu/ulima/is2/viajes
├── cajablanca/                     (a implementar) PoliticaCancelacionTest
├── cajanegra/                      (a implementar) RegistrarClienteTest
├── mocks/ReservaServiceTest        Configuración de Mockito + un caso de ejemplo
├── mutacion/CalculadoraPrecioTest  Suite heredada con alta cobertura... ¿y buena calidad?
└── refactoring/                    Pruebas de caracterización (golden master) de reportes/
```

## Flujo de una reserva

```
App ──> ReservaService.reservar(...)
          ├─ ClienteRepository.buscarPorDocumento
          ├─ PaqueteRepository.buscarPorCodigo
          ├─ CalculadoraPrecio.calcular
          ├─ PasarelaPago.cobrar              (servicio externo)
          ├─ PaqueteRepository.guardar        (cupos descontados)
          ├─ ReservaRepository.guardar        (reserva CONFIRMADA)
          └─ Notificador.enviarConfirmacion   (servicio externo)
```

Todas las dependencias de `ReservaService` se reciben por el constructor (inyección de
dependencias), incluido el `Clock`. Por eso pueden reemplazarse por *mocks* en las pruebas unitarias.
