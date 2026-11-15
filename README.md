# TusViajes - Backend REST API

Backend de la plataforma **TusViajes**, desarrollado con Java 21 y Spring Boot 4.

---

## 🛠 Tecnologías

- **Lenguaje:** Java 21
- **Framework Principal:** [Spring Boot 4.1.1](https://spring.io/projects/spring-boot)
  - Spring MVC (construcción de API REST)
  - Spring Data JPA (persistencia de datos con Hibernate)
  - Spring Security (autenticación y autorización)
  - Spring Validation (validación de datos de entrada)
- **Bases de Datos:**
  - [PostgreSQL 16](https://www.postgresql.org/) (entorno local y producción)
  - [H2 Database](https://www.h2database.com/) (base de datos en memoria para testing)
- **Seguridad y Tokens:**
  - JWT (JSON Web Tokens) con biblioteca `io.jsonwebtoken` (`jjwt` 0.12.6) para autenticación sin estado y refresh tokens
- **Documentación de API:**
  - [Springdoc OpenAPI UI](https://springdoc.org/) (OpenAPI 3 / Swagger UI)
- **Testing:**
  - JUnit 5 & Mockito
  - Spring Security Test & MockMvc
  - [Testcontainers](https://testcontainers.com/) (PostgreSQL 16) para pruebas de integración con contenedores reales
- **Calidad de Código y Cobertura:**
  - [JaCoCo](https://www.jacoco.org/) (reporte de cobertura)
  - [SonarCloud](https://sonarcloud.io/)
- **Herramientas de Build y Contenedores:**
  - Apache Maven (con Maven Wrapper `mvnw` y `mvnw.cmd`)
  - Docker & Docker Compose
- **Otras Librerías:**
  - Project Lombok
  - Jackson Datatype JSR310

---

## 📋 Prerrequisitos

Para levantar el proyecto y trabajar en él, se requiere contar con:

1. **Java Development Kit (JDK) 21** o superior instalado y configurado en la variable de entorno `JAVA_HOME`.
2. **Git** para el control de versiones.
3. **Docker & Docker Compose** (o Docker Desktop) en ejecución (requerido para ejecutar PostgreSQL en contenedor y para los tests de integración).
4. **PostgreSQL 16** (opcional si se utiliza Docker para levantar la base de datos).
5. **Maven 3.9+** (opcional, el repositorio incluye los ejecutables del wrapper `./mvnw` y `mvnw.cmd`).

---

## 🧪 Cómo ejecutar los tests

### Prerrequisitos para los tests

- **Docker Desktop / Docker Engine:** Debe estar **iniciado y en ejecución**, ya que los tests de integración (`Testcontainers`) levantan automáticamente un contenedor de PostgreSQL (`postgres:16-alpine`) para validar los componentes contra una base de datos real.

> [ACLARACION]
> Los tests que emplean `@Testcontainers` cuentan con `disabledWithoutDocker = true`, por lo que se omitirán si Docker no está corriendo. Sin embargo, para la ejecución completa y la generación precisa del reporte de cobertura se requiere que Docker esté activo.

### Comandos de ejecución

- **Ejecutar todos los tests:**
  - En Linux / macOS:
    ```bash
    ./mvnw clean test
    ```
  - En Windows (PowerShell / CMD):
    ```powershell
    .\mvnw.cmd clean test
    ```

- **Ejecutar tests y verificar cobertura (JaCoCo):**
  - En Linux / macOS:
    ```bash
    ./mvnw clean verify
    ```
  - En Windows (PowerShell / CMD):
    ```powershell
    .\mvnw.cmd clean verify
    ```
  Una vez finalizado, el reporte HTML de cobertura se encuentra en:
  `target/site/jacoco/index.html`

- **Ejecutar una clase de test específica:**
  ```bash
  ./mvnw test -Dtest=NombreDeLaClaseTest
  # Ejemplo en Windows:
  .\mvnw.cmd test -Dtest=TravelPackageServiceTest
  ```

---

## 🚀 Cómo levantar la app

### Stack completo con Docker Compose

El Compose global se mantiene en el repositorio [tusViajes-frontend](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose). Levanta **PostgreSQL 16 → API de vuelos → Backend → Frontend**, esperando que cada dependencia esté saludable. Solo requiere Git y Docker con Compose v2.17 o superior y contenedores Linux; no requiere Java ni Maven en la máquina.

Clonar el frontend como carpeta hermana de este repositorio. Desde la raíz del backend:

```bash
docker compose --project-directory ../tusViajes-frontend -f ../tusViajes-frontend/compose.yaml up -d --build --wait --wait-timeout 240
```

Por defecto, el frontend queda en http://localhost:5173 y Swagger en http://localhost:8080/swagger-ui/index.html. Los puertos, las credenciales y las imágenes se configuran en el `.env` del frontend, tomando su `.env.example` como referencia.

Las APIs usan imágenes publicadas desde `main`; este comando no compila los cambios locales del backend. Para desarrollar el backend, usar las opciones siguientes. El Compose global crea su propia red y un volumen persistente compartido por ambas APIs. La [guía del frontend](https://github.com/PDes2026C2/tusViajes-frontend#stack-completo-con-docker-compose) detalla la configuración, los datos iniciales, los logs, la actualización y cómo detener o reiniciar el entorno.

### Opción 1: Ejecución local con Maven

1. **Asegurar que la base de datos PostgreSQL esté corriendo:**
   Si no posees una instancia local de PostgreSQL activa, puedes levantar una rápidamente con Docker:
   ```bash
   docker run --name postgres-local -e POSTGRES_DB=postgres -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:16-alpine
   ```

2. **Configuración de Variables de Entorno (Opcional):**
   Las propiedades por defecto en `src/main/resources/application.properties` son:
   - `spring.datasource.url`: `jdbc:postgresql://localhost:5432/postgres`
   - `spring.datasource.username`: `admin`
   - `spring.datasource.password`: `password`
   - `FLIGHTS_API_BASE_URL`: `http://localhost:8081` (servicio externo de vuelos)

   Puedes sobreescribirlas mediante variables de entorno si utilizas otras credenciales o puertos.

3. **Iniciar la aplicación:**
   - En Linux / macOS:
     ```bash
     ./mvnw spring-boot:run
     ```
   - En Windows (PowerShell / CMD):
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```

   *O alternativamente empaquetando el `.jar`:*
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/tusViajes-0.0.1-SNAPSHOT.jar
   ```

### Opción 2: Ejecución con Docker Compose

El proyecto incluye un archivo `docker-compose.yaml` configurado para integrarse a una red compartida (`local-dev-network`):

1. **Crear la red externa si aún no existe:**
   ```bash
   docker network create local-dev-network
   ```

2. **Construir y levantar el contenedor:**
   ```bash
   docker compose up --build
   ```

---

## 📚 Documentación de Endpoints (Swagger / OpenAPI)

El detalle completo de los endpoints, contratos de entrada/salida, códigos de respuesta y modelos de datos no se mantiene en este README; está documentado y accesible interactivamente a través de **Swagger UI**:

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Especificación OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Autenticación en Swagger UI

Para probar endpoints protegidos desde la interfaz de Swagger:

1. Autentícate a través del endpoint `POST /api/auth/login` (o registra un usuario nuevo en `POST /api/auth/register/...`).
2. Copia el token JWT (`token`) retornado en la respuesta.
3. Haz clic en el botón verde **Authorize** (ubicado en el extremo superior derecho de Swagger UI).
4. Pega el token en el campo de texto `Value` y presiona **Authorize**.
5. Todas las llamadas que requieran autenticación enviarán automáticamente el encabezado `Authorization: Bearer <token>`.

## Observabilidad: salud y métricas de Prometheus

Actuator utiliza un puerto de gestión separado: **9091** de forma predeterminada. El Compose global mantiene ese puerto dentro de la red Docker. Se puede cambiar mediante la variable de entorno `MANAGEMENT_SERVER_PORT`.

- `GET /actuator/health`: estado de salud de la aplicación, sin detalles de sus dependencias.
- `GET /actuator/health/liveness` y `/actuator/health/readiness`: controles de actividad y disponibilidad para la orquestación de contenedores.
- `GET /actuator/prometheus`: cantidad de peticiones HTTP, tiempos de respuesta (incluidos los histogramas), métricas de la JVM y del pool de conexiones a la base de datos, etiquetadas con el nombre de la aplicación.

Al ejecutar Java localmente, estos endpoints están disponibles en `http://localhost:9091`. Los endpoints de negocio conservan su puerto y sus reglas de autorización. El puerto de gestión debe mantenerse fuera del ingress y del proxy del frontend.

Prometheus recolecta estas métricas por la red interna. El Compose global y la configuración de Prometheus, Grafana y sus alertas se mantienen en el repositorio `tusViajes-frontend`.

Para ejecutar las pruebas de integración de los endpoints de gestión, usar `./mvnw -Dtest=ObservabilityIntegrationTest test` o `.\mvnw.cmd -Dtest=ObservabilityIntegrationTest test` en Windows. Las pruebas requieren Docker y utilizan una base PostgreSQL aislada mediante Testcontainers.

## Observabilidad: logs y correlación de solicitudes

La aplicación escribe logs JSON en formato ECS en la salida estándar, con fecha, nivel, nombre del servicio (`tusViajes`), logger y mensaje. No hace falta instalar otra biblioteca de logging. Por defecto, la impresión directa de SQL está desactivada para conservar la salida de la aplicación en formato estructurado.

Cada solicitud HTTP de negocio recibe un identificador `X-Request-ID`. Si el cliente envía un UUID canónico válido, se reutiliza normalizado a minúsculas; si falta o es inválido, se genera uno nuevo. El mismo identificador se devuelve en el encabezado de la respuesta y se agrega como `requestId` a los logs emitidos durante el procesamiento síncrono de la solicitud. El contexto se restaura al finalizar, incluso cuando ocurre una excepción.

El registro `Solicitud HTTP finalizada` agrega los siguientes campos:

| Campo | Contenido |
|---|---|
| `requestId` | Identificador de correlación de la solicitud. |
| `httpMethod` | Método HTTP. |
| `httpRoute` | Plantilla de la ruta del controlador; `UNMAPPED` si no se resolvió uno, por ejemplo ante un rechazo de seguridad. |
| `httpStatus` | Código de respuesta HTTP. |
| `durationMs` | Tiempo de procesamiento de esta API en milisegundos. |

Las respuestas 2xx y 3xx se registran con nivel `INFO`, las 4xx con `WARN` y las 5xx con `ERROR`. Si una excepción sale sin resolver de la cadena de filtros, se registra estado 500. El registro HTTP no incluye el cuerpo, los encabezados de autenticación ni los parámetros de consulta; utiliza plantillas de ruta para evitar registrar identificadores del recurso en la URL. Las rutas de Actuator quedan excluidas del registro de solicitudes.

`FlightsApiClient` propaga el `X-Request-ID` del contexto actual en sus llamadas a la API de vuelos. Así se pueden buscar los registros de ambas APIs usando un mismo ID. Las llamadas realizadas fuera de una solicitud HTTP no reutilizan identificadores de solicitudes anteriores.

Para comprobarlo con la aplicación levantada:

```bash
curl -i -H "X-Request-ID: 09c409d2-cf9b-4a33-a5f5-388dcf4de99e" "http://localhost:8080/api/countries"
```

En PowerShell, usar `curl.exe` en lugar de `curl`. La respuesta debe incluir ese ID y la consola debe mostrar el registro JSON correspondiente. Si se usa el Compose global, adaptar el puerto a su configuración y consultar los logs desde el repositorio `tusViajes-frontend`, conservando las opciones con las que se levantó el entorno.

Las pruebas de `ObservabilityIntegrationTest` comprueban el formato JSON, la correlación entre logs y respuestas, el tratamiento de IDs ausentes o inválidos, las respuestas de error y el aislamiento entre solicitudes concurrentes. Requieren Docker para levantar PostgreSQL con Testcontainers.

`FlightsApiClientTest` verifica además la propagación del contexto actual y que el cliente no conserve un ID anterior cuando el contexto cambia o deja de existir.

## Observabilidad: trazas distribuidas

La instrumentación HTTP de Spring Boot y OpenTelemetry conserva el contexto W3C recibido en `traceparent` y genera una traza nueva cuando no hay un contexto válido. La respuesta incluye `X-Trace-ID` y los registros de la solicitud agregan `traceId` y `spanId`. `X-Request-ID` mantiene su función de correlación de logs y sigue siendo independiente del identificador de traza.

Cada operación HTTP instrumentada produce un span con su inicio, duración, resultado y relación con otras operaciones. La exportación de trazas está desactivada de forma predeterminada, para poder ejecutar la API sin un colector. Se controla con las siguientes variables:

| Variable | Uso |
|---|---|
| `TRACING_EXPORT_ENABLED` | `true` activa la exportación OTLP. Su valor predeterminado es `false`. |
| `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` | Endpoint HTTP completo del colector. Por defecto, `http://localhost:4318/v1/traces`; el Compose global utiliza `http://tempo:4318/v1/traces` por su red interna. |
| `TRACING_SAMPLING_PROBABILITY` | Proporción de trazas nuevas a muestrear, entre `0.0` y `1.0`. Java local usa `0.1` de forma predeterminada; el Compose de desarrollo usa `1.0` para facilitar las pruebas. |

El contexto de una traza puede existir en los logs aunque no se exporte o no se muestree. Para encontrar todas las solicitudes de una prueba en Tempo, activar la exportación y usar muestreo `1.0`. Los logs conservan Loki y las métricas conservan Prometheus como canales de almacenamiento.

`FlightsApiClient` utiliza el `RestClient.Builder` autoconfigurado por Spring Boot. Así, la llamada a vuelos crea un span cliente y propaga el contexto W3C; vuelos continúa la misma traza con su propio span servidor. El cliente conserva además la propagación de `X-Request-ID`.

Tempo y su fuente de Grafana se configuran en el repositorio `tusViajes-frontend`. En ese entorno se puede copiar `X-Trace-ID` desde una respuesta y buscarlo en **Explore → Tempo**. Los logs de Loki que contienen `traceId` incluyen el enlace **Ver traza**; desde un span también se pueden abrir los logs de esa traza.

Las pruebas HTTP de `ObservabilityIntegrationTest` verifican el contexto W3C, los IDs nuevos ante contextos inválidos o ausentes, la correlación con los logs y el aislamiento de solicitudes concurrentes. Se ejecutan con Docker y PostgreSQL mediante Testcontainers, sin requerir Tempo ni exportar datos a un servicio externo.

También se comprueba que el cliente de vuelos envíe el encabezado `traceparent` usando el contexto activo.
