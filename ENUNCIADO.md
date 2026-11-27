# Laboratorio 08: Pruebas de software y refactoring

**Curso:** Ingeniería de Software II
**Sistema bajo prueba:** *ULIMA Tours*, aplicación de gestión de una agencia de viajes (Java 17 + Maven). Ver [`README.md`](README.md)
**Herramientas:** JUnit 5, Mockito, JaCoCo, PIT (pitest)
**Modalidad:** Grupal (máximo 3 integrantes)

## Objetivos

1. Diseñar pruebas de **caja blanca** con la técnica del **camino básico** (complejidad ciclomática).
2. Diseñar pruebas de **caja negra** con la técnica de **partición equivalente**.
3. Escribir **pruebas unitarias aisladas con dobles de prueba (stubs)** usando Mockito.
4. Medir la **calidad de una suite de pruebas** con **pruebas de mutación** (PIT) y compararla con la cobertura de código.
5. Identificar **code smells** y aplicar **técnicas de refactoring** de forma segura, protegidas por pruebas.

## Preparación

1. Instale JDK 17 o superior y Maven 3.9+.
2. Ejecute `mvn test`: debe terminar en `BUILD SUCCESS` (19 pruebas).
3. Ejecute `mvn -q compile exec:java` y use la aplicación unos minutos: liste paquetes, cotice un
   paquete con distintas cantidades de pasajeros y categorías, registre un cliente, haga una reserva
   (tarjeta de 16 dígitos), cancélela, emita una boleta y una factura y vea el reporte de ventas.
4. Revise la estructura del proyecto descrita en el `README.md`.

> **Regla general:** salvo en la Parte E, **no modifique `src/main`**. Si encuentra un defecto,
> no lo corrija: documéntelo (ID, entrada, resultado esperado, resultado obtenido) y marque la prueba
> que lo evidencia con `@Tag("defecto")`. Esas pruebas se excluyen de `mvn test` y de PIT, y se
> ejecutan con `mvn test -Dexcluir.grupos=ninguno -Dgroups=defecto`.

---

## Parte A — Caja blanca: camino básico

### Método bajo prueba

`pe.edu.ulima.is2.viajes.servicio.PoliticaCancelacion#porcentajeReembolso(Reserva, LocalDate)`

Determina qué porcentaje del monto pagado se devuelve al cliente cuando cancela una reserva.

```java
public int porcentajeReembolso(Reserva reserva, LocalDate fechaCancelacion) {
    if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
        return 0;
    }
    long diasAntes = ChronoUnit.DAYS.between(fechaCancelacion, reserva.getPaquete().getFechaSalida());
    if (diasAntes < 0) {
        return 0;
    }
    if (reserva.isSeguroCancelacion()) {
        return 100;
    }
    CategoriaCliente categoria = reserva.getCliente().getCategoria();
    if (diasAntes >= DIAS_ANTICIPACION_ALTA) {                 // 30
        return 90;
    }
    if (diasAntes >= DIAS_ANTICIPACION_MEDIA) {                // 15
        if (categoria == CategoriaCliente.VIP) {
            return 75;
        }
        return 50;
    }
    if (diasAntes >= DIAS_ANTICIPACION_MINIMA && categoria != CategoriaCliente.REGULAR) {   // 7
        return 25;
    }
    return 0;
}
```

### Actividades

| #  | Actividad |
|----|-----------|
| A1 | Numere las sentencias/bloques del código e indique qué agrupa cada nodo. **Las condiciones compuestas (`&&`, `\|\|`) deben separarse en un nodo por cada condición simple.** |
| A2 | Dibuje el **grafo de flujo** (nodos, aristas, nodos predicado y regiones). |
| A3 | Calcule la **complejidad ciclomática V(G)** por los **tres** métodos: `V(G) = E − N + 2`, `V(G) = P + 1` y número de regiones. Los tres resultados deben coincidir. |
| A4 | Liste un **conjunto básico de caminos independientes** (tantos como V(G)). Cada camino debe introducir al menos una arista nueva. |
| A5 | Diseñe **un caso de prueba por camino**: datos de entrada (estado de la reserva, seguro, categoría del cliente, días entre la cancelación y la salida), resultado esperado y camino recorrido. Indique si algún camino es infactible. |
| A6 | Implemente los casos en `src/test/java/pe/edu/ulima/is2/viajes/cajablanca/PoliticaCancelacionTest.java`. Adjunte la captura de la cobertura de **ramas** de `PoliticaCancelacion` en el reporte de JaCoCo (`target/site/jacoco/index.html`). |

Formato sugerido para A5:

| ID   | Camino | Entrada | Resultado esperado |
|------|--------|---------|--------------------|
| CB01 | …      | …       | …                  |

---

## Parte B — Caja negra: partición equivalente

### Funcionalidad bajo prueba: *Registrar cliente*

Desde el menú (opción 3) el usuario ingresa los datos del titular. La aplicación los envía como texto a:

```java
Cliente RegistroClienteService.registrar(String tipoDocumento, String numeroDocumento,
                                         String nombres, String email, String fechaNacimiento)
```

Si los datos son válidos, el cliente se guarda con categoría `REGULAR` y se devuelve. Si no, se lanza
`ValidacionException` con el nombre del campo que falló (`getCampo()`: `tipoDocumento`,
`numeroDocumento`, `nombres`, `email` o `fechaNacimiento`) y **no se guarda nada**.

### Especificación

| Regla | Campo | Descripción |
|-------|-------|-------------|
| R1 | Tipo de documento | Obligatorio. `DNI` o `PASAPORTE`, sin distinguir mayúsculas. Cualquier otro valor es inválido. |
| R2 | Número de documento | Obligatorio; se ignoran los espacios al inicio y al final. **DNI:** exactamente **8 dígitos**. **Pasaporte:** de **6 a 12** caracteres alfanuméricos (letras A–Z sin tildes y dígitos); se guarda en **mayúsculas**. |
| R3 | Número de documento | No puede existir otro cliente con el mismo tipo y número de documento. En pasaportes la comparación **no distingue mayúsculas/minúsculas**. |
| R4 | Nombres | Obligatorio. Sin contar espacios al inicio y al final, entre **2 y 60** caracteres. Solo letras (se aceptan tildes, ñ, ü) y espacios. |
| R5 | Correo | Obligatorio. Formato `usuario@dominio.ext` (el dominio debe tener al menos un punto). Se guarda en minúsculas. |
| R6 | Fecha de nacimiento | Obligatoria. Formato **`AAAA-MM-DD`**, debe existir en el calendario y el titular debe tener **al menos 18 años cumplidos** a la fecha de hoy (quien cumple 18 hoy sí puede registrarse). |

### Actividades

| #  | Actividad |
|----|-----------|
| B1 | Identifique las **condiciones de entrada** a partir de la especificación. |
| B2 | Construya la **tabla de clases de equivalencia**: condición de entrada, clases válidas y clases inválidas, cada una con un identificador (V1, V2…, NV1, NV2…). Note que la regla R2 depende del valor de R1. |
| B3 | Diseñe los **casos de prueba**: los casos válidos deben cubrir **la mayor cantidad posible** de clases válidas a la vez; cada caso inválido debe cubrir **una sola** clase inválida (el resto de datos, válidos). Complemente con **valores límite** donde la especificación define rangos. |
| B4 | Implemente los casos en `src/test/java/pe/edu/ulima/is2/viajes/cajanegra/RegistrarClienteTest.java`. Use `ClienteRepositoryEnMemoria` y un reloj fijo (`Clock.fixed(...)`) para que las pruebas con edades no dependan del día en que se ejecutan. Se recomienda `@ParameterizedTest` con `@CsvSource` para los casos inválidos. |

Formatos sugeridos:

| Condición de entrada | Clases válidas | Clases inválidas |
|----------------------|----------------|------------------|
| …                    | V1: …          | NV1: …           |

| ID   | Clases cubiertas | tipoDocumento | numeroDocumento | nombres | email | fechaNacimiento | Resultado esperado |
|------|------------------|---------------|-----------------|---------|-------|-----------------|--------------------|
| CN01 | …                | …             | …               | …       | …     | …               | …                  |

---

## Parte C — Pruebas unitarias con Mocks: stubs 

### Clase bajo prueba: `ReservaService`

`ReservaService` coordina 8 colaboradores: tres repositorios, `CalculadoraPrecio`,
`PoliticaCancelacion`, la **pasarela de pagos** (`PasarelaPago`), el **notificador** (`Notificador`) y
un `Clock`. La implementación real de la pasarela (`PasarelaPagoSimulada`) tarda varios segundos y
rechaza pagos al azar, por lo que **no puede usarse en una prueba unitaria**.

El objetivo es probar **solo la lógica de `ReservaService`**, reemplazando todos sus colaboradores por
objetos de Mockito usados como **stubs**: se les **programa la respuesta** que deben dar
(`when(...).thenReturn(...)`, `thenAnswer`, `thenThrow`, `doThrow(...).when(...)`) y la prueba verifica
únicamente el **resultado observable** del método: el valor devuelto, la excepción lanzada y el estado de
los objetos (`Reserva`, `PaqueteTuristico`). **No** se usan `verify`, `never`, `ArgumentCaptor` ni `InOrder`.

El archivo `src/test/java/pe/edu/ulima/is2/viajes/mocks/ReservaServiceTest.java` ya contiene la
configuración y un caso resuelto (**MK01**) que debe usar como modelo.

### Reglas de negocio

**Reservar** — `reservar(tipoDoc, numeroDoc, codigoPaquete, pasajeros, conSeguro, numeroTarjeta)` → devuelve la reserva

| Regla | Descripción |
|-------|-------------|
| RN1 | Si el cliente no está registrado, se lanza `ReservaException`. |
| RN2 | Si el paquete no existe, se lanza `ReservaException`. |
| RN3 | Solo se puede reservar un paquete cuya fecha de salida sea **posterior** a hoy; si no, `ReservaException`. |
| RN4 | Si no hay cupos suficientes para todos los pasajeros, se lanza `ReservaException` y los cupos del paquete no cambian. |
| RN5 | El monto de la reserva es el que determina `CalculadoraPrecio` según el paquete, los pasajeros, la categoría del cliente y el seguro. |
| RN6 | Si la pasarela **rechaza** el cobro, se lanza `PagoRechazadoException` cuyo mensaje incluye el motivo del rechazo, y los cupos del paquete **no** cambian. |
| RN7 | Si el cobro es aprobado, se devuelve la reserva **tal como la devuelve el repositorio** (con su id), en estado `CONFIRMADA`, con el código de autorización de la pasarela, la fecha de hoy, el seguro elegido, y los cupos del paquete se descuentan. |
| RN8 | Si el envío de la notificación falla (el notificador lanza una excepción), la reserva **igual se devuelve confirmada**. |

**Cancelar** — `cancelar(idReserva)` → devuelve el monto reembolsado

| Regla | Descripción |
|-------|-------------|
| RN9  | Si la reserva no existe o no está `CONFIRMADA`, se lanza `ReservaException`. |
| RN10 | El porcentaje lo determina `PoliticaCancelacion` (fecha de hoy). Reembolso = monto total × porcentaje / 100, redondeado a 2 decimales. |
| RN11 | Si el reembolso es mayor que cero, se solicita a la pasarela. Si es cero, **no se recurre a la pasarela**: la cancelación procede aunque la pasarela no esté disponible. |
| RN12 | Si la pasarela rechaza el reembolso, se lanza `PagoRechazadoException`; la reserva **sigue confirmada** y los cupos no se liberan. |
| RN13 | Si todo sale bien: la reserva pasa a `CANCELADA`, se liberan sus cupos y se devuelve el monto reembolsado (aunque falle la notificación). |

### Actividades

| #  | Actividad |
|----|-----------|
| C1 | Explique con sus palabras qué es un **doble de prueba** y la diferencia entre **stub**, **mock**, **spy** y **fake**. Identifique en el proyecto un ejemplo de *fake* ya existente y justifique por qué **no** conviene usar `PasarelaPagoSimulada` en las pruebas. |
| C2 | Diseñe una tabla de casos de prueba (**mínimo 10**, incluyendo MK01) que cubra **todas** las reglas RN1–RN13. Para cada caso indique: reglas cubiertas, qué respuestas se programan en los stubs y qué resultado observable se verifica (valor devuelto, excepción y/o estado). |
| C3 | Implemente los casos en `ReservaServiceTest`. En el conjunto de pruebas debe usar, al menos una vez cada uno: `when(...).thenReturn(...)`, `thenAnswer(...)`, `thenThrow(...)` o `doThrow(...).when(...)` (para métodos `void`), un *argument matcher* (`any()`, `anyInt()`, `eq(...)`…) y `assertThrows`. |
| C4 | Responda: (a) ¿qué devuelve un método de un mock de Mockito al que **no** se le programó respuesta? ¿Cómo aprovecha eso, por ejemplo, el caso de RN4 para detectar que el código intentó cobrar? (b) ¿Por qué `MockitoExtension` falla con `UnnecessaryStubbingException` cuando se programa un stub que no se usa y qué le dice eso sobre el diseño del caso? (c) ¿Qué comportamientos de `ReservaService` **no** pueden comprobarse solo con stubs (por ejemplo, que se envió la notificación)? ¿Qué técnica usaría para comprobarlos? |

Formato sugerido para C2:

| ID   | Reglas | Stubs (Arrange) | Acción (Act) | Resultado esperado (Assert) |
|------|--------|-----------------|--------------|-----------------------------|
| MK01 | RN5, RN7 | cliente y paquete existen; calculadora → 2813.00; pasarela aprueba "AUT123"; repositorio asigna id 1 | `reservar(...)` 2 pasajeros | reserva id 1, `CONFIRMADA`, código "AUT123", monto 2813.00, fecha hoy; cupos 10 → 8 |

---

## Parte D — Pruebas de mutación

### Clase bajo prueba: `CalculadoraPrecio`

```java
BigDecimal calcular(PaqueteTuristico paquete, int pasajeros, CategoriaCliente categoria, boolean conSeguro)
```

| Regla | Descripción |
|-------|-------------|
| P1 | La cantidad de pasajeros debe estar entre **1 y 20**; si no, `IllegalArgumentException`. |
| P2 | Subtotal = precio por persona × pasajeros. |
| P3 | **Temporada alta** (salida en diciembre, enero, febrero o julio): recargo del **15 %** sobre el subtotal. |
| P4 | Descuento por grupo: **5 %** desde 4 pasajeros; **10 %** desde 8 pasajeros. |
| P5 | Descuento por categoría: `FRECUENTE` **3 %**, `VIP` **7 %**. |
| P6 | Los descuentos P4 y P5 se **suman** y se aplican una sola vez sobre el subtotal (ya con recargo). El descuento total **no puede superar el 15 %**. |
| P7 | Seguro de cancelación (opcional): **S/ 35.00** por pasajero en destinos nacionales y **S/ 60.00** en internacionales. Se suma al final y **no** recibe descuento. |
| P8 | El total se redondea a 2 decimales (`HALF_UP`). |

El proyecto incluye `src/test/java/pe/edu/ulima/is2/viajes/mutacion/CalculadoraPrecioTest.java`, una
suite "heredada" que ejecuta todo el código de la calculadora.

### Cómo ejecutar PIT

```bash
# Toda la aplicación (reporte en target/pit-reports/index.html)
mvn test-compile org.pitest:pitest-maven:mutationCoverage

# Solo una clase
mvn test-compile org.pitest:pitest-maven:mutationCoverage -Dpit.clases=pe.edu.ulima.is2.viajes.servicio.CalculadoraPrecio
```

### Actividades

| #  | Actividad |
|----|-----------|
| D1 | Ejecute `mvn test` y registre la cobertura de **líneas** y de **ramas** de `CalculadoraPrecio` según JaCoCo. Luego ejecute PIT sobre esa clase y registre el **mutation score**. Explique la diferencia. ¿Qué mide cada métrica? |
| D2 | Para **al menos 6 mutantes sobrevivientes** (de al menos 3 operadores de mutación distintos) complete la tabla: línea, operador, cambio realizado, por qué sobrevivió y qué caso de prueba lo mata. |
| D3 | Mejore `CalculadoraPrecioTest` (mismo archivo) hasta alcanzar un mutation score de **al menos 90 %** en `CalculadoraPrecio`. Use **valores límite** derivados de las reglas P1–P7. Adjunte la captura del reporte de PIT antes y después. |
| D4 | Si quedan mutantes vivos, analice si son **mutantes equivalentes** (no existe ninguna entrada que distinga el mutante del original). Demuéstrelo con un razonamiento sobre los valores posibles. |
| D5 | Ejecute PIT sobre **toda la aplicación**, con sus pruebas de las Partes A, B y C ya implementadas. Reporte el mutation score de `PoliticaCancelacion`, `RegistroClienteService` y `ReservaService`. Elija 2 mutantes sobrevivientes de esas clases, explique qué debilidad de su suite revelan y corríjala. ¿Algún mutante sobreviviente coincide con un defecto que usted documentó? Explique la relación. |

Formato sugerido para D2:

| # | Línea | Operador (mutator) | Cambio | ¿Por qué sobrevive? | Caso que lo mata |
|---|-------|--------------------|--------|---------------------|------------------|
| 1 | …     | `ConditionalsBoundaryMutator` | `>=` → `>` | … | `calcular(..., 4, ...)` debe dar … |

---

## Parte E — Code smells y refactoring

### Código bajo análisis: paquete `pe.edu.ulima.is2.viajes.reportes`

- `GeneradorComprobante#generar(...)`: genera el texto de una boleta o factura electrónica.
- `ReporteVentas#generar(...)`: genera el reporte mensual de ventas y la comisión de los vendedores.

Ambas clases funcionan correctamente, pero son difíciles de leer y de modificar. El proyecto incluye
**pruebas de caracterización** (*golden master*) en `src/test/java/pe/edu/ulima/is2/viajes/refactoring/`
que fijan el comportamiento actual: **son su red de seguridad**.

> En esta parte sí puede modificar `src/main`, **solo** dentro del paquete `reportes` (puede crear
> clases nuevas en él). **No** cambie los textos esperados de las pruebas de caracterización; si
> cambia la firma de un método, solo puede adaptar la forma en que las pruebas lo invocan.

### Actividades

| #  | Actividad |
|----|-----------|
| E1 | Identifique **al menos 8 code smells** (de al menos 6 tipos distintos del catálogo de Fowler) en el paquete `reportes`. Para cada uno: nombre del smell, ubicación (clase y líneas), evidencia en el código, consecuencia para el mantenimiento y técnica de refactoring que propone. |
| E2 | Calcule **antes** de refactorizar, para `GeneradorComprobante#generar` y `ReporteVentas#generar`: líneas de código, V(G) y número de parámetros. |
| E3 | Aplique **al menos 6 técnicas de refactoring distintas** (p. ej. *Extract Method*, *Replace Magic Number with Symbolic Constant*, *Rename Variable*, *Introduce Parameter Object*, *Replace Conditional with Polymorphism* / *Strategy*, *Consolidate Duplicate Conditional Fragments*, *Remove Dead Code*, *Decompose Conditional*, *Replace Primitive with Object*, *Move Method*). Haga **un commit por refactoring**, con un mensaje que nombre la técnica; ejecute `mvn test` antes de cada commit. |
| E4 | Recalcule las métricas de E2 **después** de refactorizar y preséntelas en una tabla comparativa. |
| E5 | Ejecute PIT sobre el paquete `reportes` (`-Dpit.clases=pe.edu.ulima.is2.viajes.reportes.*`) antes y después del refactoring. ¿Cambió el mutation score? ¿Qué mutantes sobreviven y qué revelan sobre las pruebas de caracterización? |
| E6 | Reflexione: ¿por qué es riesgoso refactorizar sin pruebas? ¿Por qué se usaron pruebas de caracterización en lugar de pruebas basadas en la especificación? ¿Qué refactoring **no** se atrevería a hacer solo con estas pruebas? |

Formato sugerido para E1:

| # | Code smell | Ubicación | Evidencia | Consecuencia | Refactoring propuesto |
|---|------------|-----------|-----------|--------------|-----------------------|
| 1 | Long Method | `GeneradorComprobante#generar`, líneas 20–125 | … | … | Extract Method |

---

## Entregables

1. Documento PDF con las respuestas de A1–A5, B1–B3, C1, C2, C4, D1, D2, D4, D5 y E1, E2, E4–E6
   (el grafo puede hacerse a mano, con draw.io o con Mermaid), incluyendo la **tabla de defectos**
   encontrados, si los hubiera.
2. Repositorio con el proyecto: pruebas implementadas y refactoring de la Parte E con su historial de
   commits. `mvn test` debe terminar en `BUILD SUCCESS`.
3. Capturas de los reportes de JaCoCo y PIT indicados.

## Recomendaciones

- Use `@DisplayName` para que el nombre de cada prueba indique su ID (CB01, CN01, MK01…) y el camino,
  las clases o las reglas que cubre.
- Para fechas relativas use `HOY.plusDays(n)` / `HOY.minusDays(n)` sobre una fecha fija.
- Compare montos con `assertEquals(new BigDecimal("1150.00"), total)`: `BigDecimal.equals` también
  compara la escala (`1150.0` ≠ `1150.00`).
- En Mockito, el modo estricto (`MockitoExtension`) falla si programa un *stub* que la prueba no usa
  (`UnnecessaryStubbingException`): es una señal de que el caso está mal diseñado. Use `lenient()`
  solo si lo puede justificar (por ejemplo, un stub que el código correcto **no** debería llegar a usar).
- Un mock sin respuesta programada devuelve valores por defecto: `null`, `0`, `false`,
  `Optional.empty()` o una lista vacía.
- PIT aborta si alguna prueba falla sin mutar el código: verifique que `mvn test` esté en verde antes
  de ejecutarlo.
