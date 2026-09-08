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
| `GET` | `/api/travel-packages` | Public | Get all travel packages |
| `GET` | `/api/travel-packages/{id}` | Public | Get travel package by ID |
| `POST` | `/api/travel-packages` | Authenticated | Create a new travel package |
| `PUT` | `/api/travel-packages/{id}` | Authenticated | Update travel package details |
| `DELETE` | `/api/travel-packages/{id}` | Authenticated | Delete travel package |

### Hotels (`/api/hotels`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/hotels` | `ADMIN`, `AGENCY` | Get all hotels |
| `GET` | `/api/hotels/{id}` | `ADMIN`, `AGENCY` | Get hotel by ID |
| `POST` | `/api/hotels` | Authenticated | Register a new hotel |

### Buyers (`/api/buyers`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/buyers` | Authenticated | Get all buyers |
| `GET` | `/api/buyers/{id}` | Authenticated | Get buyer by ID |
| `POST` | `/api/buyers` | Public | Register a buyer |
| `POST` | `/api/buyers/{buyerId}/favorites/{travelPackageId}` | Authenticated | Add travel package to buyer's favorites |
| `DELETE` | `/api/buyers/{buyerId}/favorites/{travelPackageId}` | Authenticated | Remove travel package from buyer's favorites |
| `GET` | `/api/buyers/{buyerId}/favorites` | Authenticated | Get all favorite travel packages for a buyer |

### Agencies (`/api/agencies`)
| Method | Path | Access | Description |
|---|---|---|---|
| `GET` | `/api/agencies` | `ADMIN` | Get all agencies |
| `GET` | `/api/agencies/{id}` | `ADMIN` | Get agency by ID |
| `PUT` | `/api/agencies/{id}` | `ADMIN` | Update agency business name |
| `DELETE` | `/api/agencies/{id}` | `ADMIN` | Delete agency |

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
