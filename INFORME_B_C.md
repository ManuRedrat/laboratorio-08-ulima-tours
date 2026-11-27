# Laboratorio 08 - Partes B y C

Sistema: ULIMA Tours. Fecha de referencia de las pruebas: 27 de noviembre de 2026.

## B1. Condiciones de entrada

El tipo y el número de documento son obligatorios; el formato del número depende del tipo y la pareja tipo-número debe ser única. El nombre, correo y fecha de nacimiento también son obligatorios. Deben respetar las longitudes, caracteres, estructura y edad mínima indicadas por R1-R6. Los espacios exteriores se retiran del número y nombre; el pasaporte se guarda en mayúsculas y el correo en minúsculas. Un registro válido se guarda como REGULAR; ante cualquier error se informa el campo y no se añade un cliente.

## B2. Clases de equivalencia

| Condición de entrada | Clases válidas | Clases inválidas |
|---|---|---|
| Tipo | V1: DNI sin distinguir mayúsculas; V2: PASAPORTE sin distinguir mayúsculas | NV1: nulo/vacío; NV2: tipo desconocido |
| Número DNI | V3: ocho dígitos, con o sin espacios exteriores | NV3: nulo/vacío; NV4: menos de ocho; NV5: más de ocho; NV6: carácter no numérico |
| Número pasaporte | V4: 6-12 caracteres ASCII alfanuméricos, con o sin espacios exteriores | NV7: menos de seis; NV8: más de doce; NV9: carácter ajeno a A-Z, a-z o 0-9; NV3: nulo/vacío |
| Unicidad | V5: no existe la pareja tipo-número | NV10: DNI repetido; NV11: pasaporte repetido sin distinguir mayúsculas |
| Nombre | V6: 2-60 letras y espacios, incluidos caracteres acentuados | NV12: nulo/vacío; NV13: menos de dos; NV14: más de 60; NV15: caracteres ajenos a letras y espacios |
| Correo | V7: usuario@dominio.ext con punto en el dominio | NV16: nulo/vacío; NV17: formato inválido |
| Nacimiento | V8: fecha real AAAA-MM-DD y 18 años cumplidos o más | NV18: nulo/vacío; NV19: fecha inexistente; NV20: formato inválido; NV21: menor de 18 años |

Las categorías de longitud son particiones independientes en cada formato de documento; los límites concretos se ejercitan por separado. La comparación de unicidad considera el tipo, por lo que un mismo texto con tipos distintos no es la misma pareja.

## B3. Casos diseñados

El reloj está fijo en 2026-11-27 UTC. En las entradas de CN01, cada `·` representa un espacio exterior. En CN22 ya existe el DNI `12345678` y en DEF01 ya existe el pasaporte `ABC123`. En las filas inválidas, el nombre del campo indica el valor esperado de `ValidacionException.getCampo()` y no se añade ningún registro.

| ID | Clases cubiertas | tipoDocumento | numeroDocumento | nombres | email | fechaNacimiento | Resultado esperado |
|---|---|---|---|---|---|---|---|
| CN01 | V1, V3, V5-V8 | `dni` | `·12345678·` | `··Ana Ñúñez··` | `ANA@CORREO.PE` | `2008-11-27` | Registra un cliente REGULAR de 18 años. Quita los espacios exteriores y guarda el correo en minúsculas. |
| CN02a | V2, V4-V8 | `pasaporte` | `ab1234` | `Al` | `a@b.pe` | `2000-02-29` | Registra `AB1234` y conserva `Al`. |
| CN02b | V2, V4-V8 | `pasaporte` | `ab1234567890` | 60 letras `A` | `a@b.pe` | `2000-02-29` | Registra `AB1234567890` y conserva las 60 letras. |
| CN03 | NV1 | `null` | `12345678` | `Ana` | `a@b.pe` | `2000-01-01` | `tipoDocumento` |
| CN04 | NV2 | `RUC` | `12345678` | `Ana` | `a@b.pe` | `2000-01-01` | `tipoDocumento` |
| CN05 | NV3 | `DNI` | `null` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN06 | NV4 | `DNI` | `1234567` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN07 | NV5 | `DNI` | `123456789` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN08 | NV6 | `DNI` | `1234567A` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN09 | NV7 | `PASAPORTE` | `A1234` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN10 | NV8 | `PASAPORTE` | `A123456789012` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN11 | NV9 | `PASAPORTE` | `ABC-123` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento` |
| CN12 | NV12 | `DNI` | `12345678` | `null` | `a@b.pe` | `2000-01-01` | `nombres` |
| CN13 | NV13 | `DNI` | `12345678` | `A` | `a@b.pe` | `2000-01-01` | `nombres` |
| CN14 | NV14 | `DNI` | `12345678` | 61 letras `A` | `a@b.pe` | `2000-01-01` | `nombres` |
| CN15 | NV15 | `DNI` | `12345678` | `Ana1` | `a@b.pe` | `2000-01-01` | `nombres` |
| CN16 | NV16 | `DNI` | `12345678` | `Ana` | `null` | `2000-01-01` | `email` |
| CN17 | NV17 | `DNI` | `12345678` | `Ana` | `ana@correo` | `2000-01-01` | `email` |
| CN18 | NV18 | `DNI` | `12345678` | `Ana` | `a@b.pe` | `null` | `fechaNacimiento` |
| CN19 | NV19 | `DNI` | `12345678` | `Ana` | `a@b.pe` | `2000-02-30` | `fechaNacimiento` |
| CN20 | NV20 | `DNI` | `12345678` | `Ana` | `a@b.pe` | `27/11/2000` | `fechaNacimiento` |
| CN21 | NV21 | `DNI` | `12345678` | `Ana` | `a@b.pe` | `2008-11-28` | `fechaNacimiento` |
| CN22 | NV10 | `DNI` | `12345678` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento`. Se conserva un cliente. |
| DEF01 | NV11 | `PASAPORTE` | `abc123` | `Ana` | `a@b.pe` | `2000-01-01` | `numeroDocumento`. Se conserva un cliente. |

## C1. Dobles de prueba

Un doble sustituye a una dependencia para controlar o observar la interacción durante una prueba. Un **stub** entrega respuestas programadas; un **mock**, en el sentido de técnica, permite establecer y verificar expectativas de interacción; un **spy** registra llamadas y puede envolver comportamiento real; un **fake** es una implementación funcional simplificada. Mockito puede crear objetos que se usan como stubs o como mocks según cómo se usen: en esta parte solo se programan respuestas y se observan resultados y estado.

`ClienteRepositoryEnMemoria` es un fake: almacena clientes realmente, pero en una lista en memoria y sin una base de datos. `PasarelaPagoSimulada` tarda y rechaza operaciones al azar, por lo que una prueba dependería del tiempo y del azar. Se sustituye por un stub determinista.

## C2. Casos de ReservaService

La fecha fija es 2026-11-27. El paquete de los casos ordinarios tiene 10 cupos y sale 40 días después; la reserva de cancelación tiene dos pasajeros y monto S/ 101.01. Los repositorios, la calculadora, la política, la pasarela y el notificador son dobles Mockito; el reloj fijo es una dependencia determinista. Los resultados de métodos no programados son los valores por defecto de Mockito.

| ID | Reglas | Stubs (Arrange) | Acción (Act) | Resultado esperado (Assert) |
|---|---|---|---|---|
| MK01 | RN5, RN7 | Cliente y paquete existentes. La calculadora devuelve 2813.00, el cobro se aprueba con `AUT123` y el repositorio asigna id 1. | `reservar(...)` para 2, sin seguro | Id 1, estado `CONFIRMADA`, autorización `AUT123`, monto 2813.00, fecha fija y cupos de 10 a 8. |
| MK02 | RN1 | La búsqueda del cliente devuelve `Optional.empty()` por defecto. | `reservar(...)` para 2, sin seguro | `ReservaException` y 10 cupos. |
| MK03 | RN2 | El cliente existe. La búsqueda del paquete devuelve `Optional.empty()` por defecto. | `reservar(...)` para 2, sin seguro | `ReservaException`. |
| MK04 | RN3 | Cliente y paquete existentes. El paquete sale hoy. | `reservar(...)` para 2, sin seguro | `ReservaException` y 10 cupos. |
| MK05 | RN4 | Cliente y paquete existentes. Queda un cupo. | `reservar(...)` para 2, sin seguro | `ReservaException` y se conserva el cupo. |
| MK06 | RN6 | Cliente y paquete existentes. La calculadora devuelve 100.00 y el cobro se rechaza por `sin fondos`. | `reservar(...)` para 2, sin seguro | `PagoRechazadoException` con el motivo y 10 cupos. |
| MK07 | RN7, RN8 | Cliente y paquete existentes. La calculadora devuelve 100.00 y el cobro se aprueba. El repositorio devuelve la reserva y el notificador lanza una excepción. | `reservar(...)` para 2, sin seguro | Reserva `CONFIRMADA` y 8 cupos. |
| MK08 | RN9 | La búsqueda de reserva devuelve `Optional.empty()` por defecto. | `cancelar(1)` | `ReservaException`. |
| MK09 | RN9 | El repositorio devuelve una reserva `CANCELADA`. | `cancelar(1)` | `ReservaException` y 10 cupos. |
| MK10 | RN10, RN13 | Reserva confirmada. La política devuelve 25 %, se aprueba el reembolso de 25.25 y el notificador lanza una excepción. | `cancelar(1)` | Devuelve 25.25 con `HALF_UP`. Queda `CANCELADA` y los cupos pasan de 10 a 12. |
| MK11 | RN12 | Reserva confirmada. La política devuelve 50 % y se rechaza el reembolso de 50.51. | `cancelar(1)` | `PagoRechazadoException`, estado `CONFIRMADA` y 10 cupos. |
| MK12 | RN5, RN7 | Cliente y paquete existentes. La calculadora devuelve 107.00, el cobro se aprueba y el repositorio devuelve una reserva específica con id 42 y seguro. | `reservar(...)` para 2, con seguro | Devuelve la misma instancia con seguro y quedan 8 cupos. |
| DEF02 | RN11 | Reserva confirmada. La política devuelve 0 % y la pasarela queda sin respuesta programada. | `cancelar(1)` | Devuelve 0.00, pasa a `CANCELADA` y libera 2 cupos sin consultar la pasarela. |

## C4. Preguntas

**a.** Un método no programado devuelve su valor predeterminado: `null` para la mayoría de referencias, cero para números, `false` para booleanos y valores vacíos para ciertos tipos como `Optional` y colecciones. En MK05 no se programa la calculadora ni el cobro: si la implementación intentara cobrar tras detectar falta de cupos, terminaría en una excepción distinta o no llegaría al resultado esperado, haciendo fallar la prueba. El estado de cupos también se verifica.

**b.** `MockitoExtension` usa stubbing estricto para señalar respuestas programadas que no se utilizaron. Ello suele revelar configuración innecesaria, argumentos incorrectos o una ruta de ejecución distinta de la prevista. Conviene preparar solo las respuestas necesarias para cada caso; no se silencia la comprobación indiscriminadamente.

**c.** Los resultados y estados no demuestran por sí solos que se llamó al notificador, cuántas veces se llamó o en qué orden ocurrieron las interacciones. Para ello se usarían mocks con `verify`, `never`, `ArgumentCaptor` o `InOrder`, según la pregunta. Esa técnica queda fuera de la restricción de esta parte. Para RN11, el stub sin respuesta hace observable la llamada indebida como fallo, pero no sustituye una verificación general de interacciones.

## Defectos encontrados

| ID | Entrada y precondición | Esperado | Observado |
|---|---|---|---|
| DEF01 | Registrar `PASAPORTE/ABC123` y luego `PASAPORTE/abc123` | Rechazar el duplicado con campo `numeroDocumento` y mantener un cliente | No se lanza ValidacionException. La búsqueda se hace antes de pasar el segundo número a mayúsculas y el repositorio compara con distinción de mayúsculas. |
| DEF02 | Cancelar una reserva confirmada con política 0% y pasarela sin respuesta | Cancelar y devolver 0.00 sin recurrir a la pasarela | Se llama a `reembolsar`; al recibir null, ocurre NullPointerException y la reserva no se cancela. El condicional del servicio admite cero. |

Ambas pruebas llevan `@Tag("defecto")`; no se modifica producción. La ejecución normal `mvn clean test` terminó con 53 pruebas, cero fallos y errores. La ejecución específica `mvn test -Dexcluir.grupos=ninguno -Dgroups=defecto` ejecutó ambas pruebas y falló como se esperaba: una aserción no satisfecha y un NullPointerException. Estas ejecuciones se realizaron con Java 27 y Maven 3.9.16.
