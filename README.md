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
