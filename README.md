# TusViajes Web Application

Backend REST API for **TusViajes**, a travel package marketplace platform built with Java 21 and Spring Boot 4.

---

## 🌟 Business Domain

The platform connects three key user roles:

1. **Agencies (`AGENCY`)**:
   - Register their business with official credentials (tax ID, business name).
   - Once authorized by an Administrator, create, publish, update, and manage travel packages (including hotel details, dates, pricing, and descriptions).

2. **Buyers (`BUYER`)**:
   - Register and explore available travel packages.
   - Save and manage favorite travel packages in their wishlist.

3. **Administrators (`ADMIN`)**:
   - Review pending agency registrations and approve or reject them.
   - Manage administrators, agencies, hotels, and oversee platform data.

---

## 🛠 Tech Stack

- **Java 21**
- **Spring Boot 4.1.1** (Spring MVC, Spring Data JPA, Spring Security, Spring Validation)
- **PostgreSQL** (Production & Docker Compose) / **H2** (In-Memory for testing)
- **JWT (JSON Web Tokens)** for stateless authentication & refresh tokens (io.jsonwebtoken / jjwt)
- **Lombok**
- **Testcontainers** (Integration testing against PostgreSQL 16)
- **JaCoCo** (Code coverage)
- **Docker & Docker Compose**

---

## 🚀 Unified REST API Endpoints

All retrieval endpoints and internal service methods follow a unified `get` convention (`getAll`, `getById`, `getPending`, `getFavorites`).

### Authentication (`/api/auth`)
| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Authenticate user (Admin, Agency, Buyer) and retrieve JWT access & refresh tokens |
| `POST` | `/api/auth/register/agency` | Public | Register a new agency (status: `PENDING`) |
| `POST` | `/api/auth/register/buyer` | Public | Register a new buyer |
| `POST` | `/api/auth/refresh` | Public | Refresh expired access token using a valid refresh token |

### Travel Packages (`/api/travel-packages`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/travel-packages` | Public | Get all travel packages (paginated, e.g. `?page&size`) |
| `GET` | `/api/travel-packages/{id}` | Public | Get travel package by ID |
| `POST` | `/api/travel-packages` | `AGENCY` | Create own travel package (agencyId from JWT, no `agencyId` in body) |
| `PUT` | `/api/travel-packages/{id}` | `AGENCY` | Update own travel package (403 if not owned, 404 if not found) |
| `DELETE` | `/api/travel-packages/{id}` | `AGENCY` | Delete own travel package (403 if not owned, 404 if not found) |

### Hotels (`/api/hotels`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/hotels` | `ADMIN`, `AGENCY` | Get all hotels |
| `GET` | `/api/hotels/{id}` | `ADMIN`, `AGENCY` | Get hotel by ID |
| `POST` | `/api/hotels` | `ADMIN` | Register a new hotel |

### Flights (`/api/flights`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/flights` | `ADMIN`, `AGENCY` | Get available flights (supports filtering & pagination) |
| `GET` | `/api/flights/{id}` | `ADMIN`, `AGENCY` | Get flight by ID |

### Cities (`/api/cities`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/cities` | Public | Get all cities (supports optional `?isoCode` filter) |

### Countries (`/api/countries`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/countries` | Public | Get all countries |

### Buyers (`/api/buyers`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/buyers` | `ADMIN` | Get all buyers |
| `GET` | `/api/buyers/{id}` | `ADMIN` | Get buyer by ID |
| `POST` | `/api/buyers/me/favorites/{travelPackageId}` | `BUYER` | Add own favorite travel package |
| `DELETE` | `/api/buyers/me/favorites/{travelPackageId}` | `BUYER` | Remove own favorite travel package |
| `GET` | `/api/buyers/me/favorites` | `BUYER` | Get own favorite travel packages |
| `GET` | `/api/buyers/me/purchases` | `BUYER` | Get own purchases (paginated, `?page&size`) |

### Agencies (`/api/agencies`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/agencies` | `ADMIN` | Get all agencies |
| `GET` | `/api/agencies/{id}` | `ADMIN` | Get agency by ID |
| `GET` | `/api/agencies/me/packages` | `AGENCY` | Get own travel packages (paginated, e.g. `?page&size`) |
| `GET` | `/api/agencies/me/sales` | `AGENCY` | Get own sales (paginated, `?page&size`, includes buyer + package) |
| `PUT` | `/api/agencies/{id}` | `AGENCY` | Update own agency business name (403 if not owned) |
| `PUT` | `/api/agencies/me` | `AGENCY` | Update own agency business name |
| `DELETE` | `/api/agencies/{id}` | `ADMIN` | Delete agency |

### Purchases (`/api/purchases` & `/api/buyers`)
| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/purchases/{travelPackageId}` | `BUYER` | Purchase a travel package |
| `GET` | `/api/buyers/me/purchases` | `BUYER` | Get own purchases (paginated, `?page&size`) |

### Reviews (`/api/reviews`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/reviews/{travelPackageId}` | `ADMIN` | Get reviews for a travel package (paginated, `?page&size&sort`) |
| `POST` | `/api/reviews/{travelPackageId}` | `BUYER` | Create a review for a finished, purchased travel package |

### Administrator Management (`/api/admin`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/administrators` | `ADMIN` | Get all administrators |
| `GET` | `/api/admin/administrators/{id}` | `ADMIN` | Get administrator by ID |
| `POST` | `/api/admin/administrators` | `ADMIN` | Create a new administrator |
| `GET` | `/api/admin/agencies/pending` | `ADMIN` | Get all pending agencies awaiting approval |
| `POST` | `/api/admin/agencies/{id}/authorize` | `ADMIN` | Authorize agency |
| `POST` | `/api/admin/agencies/{id}/reject` | `ADMIN` | Reject agency |

---

## 📚 API Documentation (Swagger / OpenAPI)

Interactive documentation and API exploration is powered by Swagger UI and OpenAPI 3:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> [!TIP]
> **Autenticación en Swagger**:
> 1. Inicie sesión mediante `POST /api/auth/login` o registre un usuario.
> 2. Copie el valor de `token`.
> 3. En la parte superior derecha de Swagger UI, haga clic en **Authorize**, pegue el token en el campo `Value` y presione **Authorize**. Todas las peticiones protegidas incluirán automáticamente la cabecera `Authorization: Bearer <token>`.

---

## 🏃 Running the Application

### Prerequisites
- JDK 21+
- Maven (or use included `mvnw`)
- Docker (optional, for local PostgreSQL container)

### Build & Run
```bash
# Build the application
./mvnw clean package

# Run the Spring Boot application
./mvnw spring-boot:run
```

The server starts on port `8080` by default.
