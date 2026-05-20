# MicroCommerce | Sistema E-commerce con Microservicios

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

---

## Descripción | Description

**ES:** MicroCommerce es una plataforma de e-commerce construida con arquitectura de microservicios, diseñada para demostrar las mejores prácticas en desarrollo de sistemas distribuidos. Implementa patrones modernos como Service Discovery, API Gateway, Configuration Management, Circuit Breaker y Caching distribuido.

**EN:** MicroCommerce is an e-commerce platform built with microservices architecture, designed to demonstrate best practices in distributed systems development. It implements modern patterns such as Service Discovery, API Gateway, Configuration Management, Circuit Breaker, and distributed Caching.

---

## Arquitectura | Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         API GATEWAY                              │
│                      (Port 8080)                                 │
│            Circuit Breaker | Load Balancer | Routing            │
└────────────────┬────────────────────────────────────────────────┘
                 │
    ┌────────────┼────────────┬────────────┬──────────────┐
    │            │            │            │              │
    ▼            ▼            ▼            ▼              ▼
┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────────┐
│Product │  │ Order  │  │  User  │  │Payment │  │   Eureka   │
│Service │  │Service │  │Service │  │Service │  │   Server   │
│ :8081  │  │ :8082  │  │ :8083  │  │ :8084  │  │   :8761    │
└───┬────┘  └───┬────┘  └───┬────┘  └───┬────┘  └────────────┘
    │           │           │           │              │
    │           │           │           │              │
┌───▼───┐   ┌───▼───┐   ┌───▼───┐   ┌───▼────┐  ┌────▼──────┐
│PostgreSQL MongoDB │   │PostgreSQL Redis   │  │   Config  │
│ Product│   │ Orders│   │  Users│   │Cache  │  │  Server   │
└────────┘   └────────┘   └────────┘   └────────┘  │  :8888    │
                                                    └───────────┘
```

---

## Stack Tecnológico | Tech Stack

### Backend Framework
- **Spring Boot 3.2.0** - Framework principal
- **Spring Cloud 2023.0.0** - Microservices tools
- **Java 17** - Lenguaje de programación

### Microservices Infrastructure
- **Eureka Server** - Service Discovery
- **Spring Cloud Config** - Centralized Configuration
- **Spring Cloud Gateway** - API Gateway
- **Resilience4j** - Circuit Breaker

### Databases
- **PostgreSQL 16** - Product, User and Payment Services
- **MongoDB** - Order Service
- **Redis 7** - Distributed Cache

### Messaging
- **RabbitMQ 3.13** - Event-Driven Communication (Order <-> Payment)

### Observability | Observabilidad
- **Elasticsearch 8.12** - Log storage and indexing
- **Logstash 8.12** - Log ingestion pipeline
- **Kibana 8.12** - Log visualization UI
- **logstash-logback-encoder** - JSON log shipping from Spring Boot

### Build & Deploy
- **Maven 3.9+** - Build tool
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Orchestration (planned)

### Documentation & Testing
- **Swagger/OpenAPI 3.0** - API Documentation
- **JUnit 5 & Mockito** - Testing (planned)
- **TestContainers** - Integration Testing (planned)

---

## Microservicios | Microservices

| Microservicio | Puerto | Base de Datos | Estado | Descripción |
|--------------|--------|---------------|--------|-------------|
| **Eureka Server** | 8761 | - | [COMPLETO] | Service Discovery |
| **Config Server** | 8888 | Git (local) | [COMPLETO] | Configuration Management |
| **API Gateway** | 8080 | - | [COMPLETO] | Routing & Load Balancing |
| **Product Service** | 8081 | PostgreSQL + Redis | [COMPLETO] | Product Catalog Management |
| **Order Service** | 8082 | MongoDB | [OPERATIVO] | Order Processing & Orchestration |
| **User Service** | 8083 | PostgreSQL | [OPERATIVO] | User & Authentication |
| **Payment Service** | 8084 | PostgreSQL + Stripe | [OPERATIVO] | Payment Processing |

---

## Quick Start

### Prerequisitos | Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL 16** (o usar Docker)
- **Redis 7** (o usar Docker)

### Paso 1: Iniciar Infraestructura (PostgreSQL + Redis)

**IMPORTANTE:** Antes de ejecutar Docker Compose, configura las variables de entorno:

```bash
# Desde la raíz del proyecto
# 1. Copia el archivo de ejemplo
cp .env.example .env

# 2. Edita .env y cambia las credenciales (especialmente para producción)
# El archivo .env NO se sube a Git (está en .gitignore)

# 3. Inicia los servicios
docker-compose -f docker-compose-dev.yml up -d
```

### Paso 2: Iniciar Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

**Verificar:** http://localhost:8761

### Paso 3: Iniciar Config Server

```bash
cd config-server
mvn spring-boot:run
```

**Verificar:** http://localhost:8888/actuator/health

### Paso 4: Iniciar API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

**Verificar:** http://localhost:8080/actuator/health

### Paso 5: Iniciar Product Service

```bash
cd product-service
mvn spring-boot:run
```

**Verificar:** http://localhost:8081/actuator/health

**Swagger UI:** http://localhost:8081/swagger-ui.html

---

## Estructura del Proyecto | Project Structure

```
MicroCommerce/
├── eureka-server/              # Service Discovery
├── config-server/              # Configuration Management
├── api-gateway/                # API Gateway
├── product-service/            # Product Catalog Microservice
│   ├── src/main/java/
│   │   └── com/microcommerce/product/
│   │       ├── controller/     # REST Controllers
│   │       ├── service/        # Business Logic
│   │       ├── repository/     # Data Access
│   │       ├── entity/         # JPA Entities
│   │       ├── dto/            # Data Transfer Objects
│   │       ├── mapper/         # Entity-DTO Mappers
│   │       ├── exception/      # Custom Exceptions
│   │       └── config/         # Configuration Classes
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── config/                     # Centralized Configurations
│   ├── application.yml
│   ├── product-service.yml
│   ├── order-service.yml
│   └── user-service.yml
├── docker-compose-dev.yml      # Docker Compose for Development
├── pom.xml                     # Parent POM
└── README.md
```

---

## Características Clave | Key Features

### [IMPLEMENTADO] | Implemented

- **Service Discovery** con Eureka
- **Centralized Configuration** con Spring Cloud Config
- **API Gateway** con Spring Cloud Gateway
- **Circuit Breaker** con Resilience4j
- **Load Balancing** client-side
- **Health Checks** con Actuator
- **Product Service** completo:
  - CRUD de productos
  - Búsqueda avanzada (nombre, categoría, precio)
  - Gestión de stock (increase/decrease)
  - Redis Caching (Cache-Aside pattern)
  - Swagger/OpenAPI documentation
  - Global Exception Handling
  - Validación de datos
  - **Suite de Tests Completa**:
    - 18 tests unitarios (Service Layer)
    - 12 tests de Mapper (MapStruct)
    - 19 tests de Controller (MockMvc)
    - Tests de integración con TestContainers
    - Cobertura >80%

### [EN DESARROLLO] | In Development

- **Order Service** (Commit 9/12 completado)
  -  Entidades Order y OrderItem
  -  Repositorios JPA con queries personalizadas
  -  DTOs con validación
  -  Service Layer con Resilience4j (próximo)
  -  REST Controller
  -  Tests unitarios e integración

### [IMPLEMENTADO] Event-Driven con RabbitMQ

- Exchanges topic: `orders.exchange`, `payments.exchange`
- Eventos publicados por **Order Service**:
  - `ORDER_CREATED` (routing key `order.created`)
  - `ORDER_CANCELLED` (routing key `order.cancelled`)
- Eventos publicados por **Payment Service**:
  - `PAYMENT_COMPLETED` (routing key `payment.completed`)
  - `PAYMENT_FAILED` (routing key `payment.failed`)
  - `PAYMENT_REFUNDED` (routing key `payment.refunded`)
- **Order Service** consume `payment.#` y actualiza el pedido a `PAID` cuando recibe `PAYMENT_COMPLETED`
- Serializacion JSON via `Jackson2JsonMessageConverter`
- Dead Letter Queue para eventos de pago rechazados
- RabbitMQ Management UI: http://localhost:15672 (guest/guest)

###  [PRÓXIMOS] | Next

- Logging centralizado con ELK
- Monitoring con Prometheus y Grafana
- API Versioning
- Despliegue en Kubernetes
- CI/CD Pipeline

---

## Endpoints Principales | Main Endpoints

### Product Service (via Gateway)

| Method | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/products` | Listar todos los productos |
| GET | `/api/products/{id}` | Obtener producto por ID |
| GET | `/api/products/active` | Listar productos activos |
| GET | `/api/products/search?name={name}` | Buscar por nombre |
| GET | `/api/products/category/{category}` | Buscar por categoría |
| GET | `/api/products/price-range?minPrice={min}&maxPrice={max}` | Buscar por rango de precio |
| POST | `/api/products` | Crear nuevo producto |
| PUT | `/api/products/{id}` | Actualizar producto |
| DELETE | `/api/products/{id}` | Eliminar producto |
| PATCH | `/api/products/{id}/decrease-stock?quantity={qty}` | Disminuir stock |
| PATCH | `/api/products/{id}/increase-stock?quantity={qty}` | Aumentar stock |

**Base URL via Gateway:** `http://localhost:8080`  
**Base URL directo:** `http://localhost:8081`

---

## Monitoreo | Monitoring

### Eureka Dashboard
```
http://localhost:8761
```
Ver todos los servicios registrados

### Spring Boot Actuator
```
http://localhost:{port}/actuator/health
http://localhost:{port}/actuator/metrics
http://localhost:{port}/actuator/info
```

### Gateway Routes
```
http://localhost:8080/actuator/gateway/routes
```

---

## Testing | Pruebas

### Crear un Producto

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop HP Pavilion",
    "description": "Laptop HP 15 pulgadas, 8GB RAM, 256GB SSD",
    "price": 899.99,
    "stock": 25,
    "category": "Electronics",
    "imageUrl": "https://example.com/laptop.jpg",
    "active": true
  }'
```

### Obtener Todos los Productos

```bash
curl http://localhost:8080/api/products
```

### Buscar por Categoría

```bash
curl http://localhost:8080/api/products/category/Electronics
```

---

## Patrones de Diseño Implementados | Design Patterns

- **Service Registry Pattern** - Eureka Server
- **API Gateway Pattern** - Spring Cloud Gateway
- **Circuit Breaker Pattern** - Resilience4j
- **Cache-Aside Pattern** - Redis caching
- **Repository Pattern** - Spring Data JPA
- **DTO Pattern** - Data Transfer Objects
- **Factory Pattern** - Entity-DTO Mappers
- **Dependency Injection** - Spring IoC

---

## Configuración | Configuration

## Variables de Entorno

Copia `.env.example` a `.env` y configura los valores segun tu entorno:
```bash
cp .env.example .env
```

Consulta `.env.example` para ver todas las variables requeridas y su descripcion.

---

## Centralized Logging | Logging Centralizado (ELK)

**ES:** MicroCommerce integra el stack ELK (Elasticsearch, Logstash, Kibana) para centralizar los logs de todos los microservicios. Los servicios envian logs estructurados en JSON a Logstash mediante `logstash-logback-encoder`.

**EN:** MicroCommerce integrates the ELK stack (Elasticsearch, Logstash, Kibana) to centralize logs from every microservice. Services emit structured JSON logs to Logstash via `logstash-logback-encoder`.

### Activacion | Enable

```bash
# 1. Arrancar el stack ELK
docker-compose -f docker-compose-dev.yml up -d elasticsearch logstash kibana

# 2. Exportar variables y arrancar los microservicios
export LOGSTASH_ENABLED=true
export LOGSTASH_HOST=localhost
export LOGSTASH_PORT=5000
```

**ES:** `LOGSTASH_ENABLED` no hace que los servicios ya arrancados empiecen a enviar logs por si solos. Cada microservicio debe iniciarse en una terminal donde esas variables ya esten definidas.

**EN:** `LOGSTASH_ENABLED` does not make already running services start shipping logs automatically. Each microservice must be started in a terminal where those variables are already defined.

**PowerShell (Windows):**

```powershell
$env:LOGSTASH_ENABLED="true"
$env:LOGSTASH_HOST="localhost"
$env:LOGSTASH_PORT="5000"
cd order-service
mvn spring-boot:run
```

### Kibana Data View

**ES:**
1. Abre `http://localhost:5601`
2. Ve a `Stack Management -> Data Views`
3. Pulsa `Create data view`
4. Usa el patron `microcommerce-*`
5. Selecciona `@timestamp` como campo temporal

**EN:**
1. Open `http://localhost:5601`
2. Go to `Stack Management -> Data Views`
3. Click `Create data view`
4. Use the pattern `microcommerce-*`
5. Select `@timestamp` as the time field

### Verificacion | Verification

```bash
# Elasticsearch indexes
curl "http://localhost:9200/_cat/indices/microcommerce-*?v"
```

```powershell
# PowerShell
Invoke-WebRequest "http://localhost:9200/_cat/indices/microcommerce-*?v" -UseBasicParsing
```

**ES:** Si no aparecen indices `microcommerce-*`, normalmente significa que el servicio no se inicio con `LOGSTASH_ENABLED=true` o que Logstash no estaba levantado al arrancarlo.

**EN:** If `microcommerce-*` indices do not appear, it usually means the service was not started with `LOGSTASH_ENABLED=true` or Logstash was not up when the service started.

### Endpoints

- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601
- Logstash (TCP input): localhost:5000

Mas detalles en [elk/README.md](./elk/README.md).

---

## Documentación Adicional | Additional Documentation

- [Product Service README](./product-service/README.md)
- [Eureka Server README](./eureka-server/README.md)
- [Config Server README](./config-server/README.md)
- [API Gateway README](./api-gateway/README.md)
- [ELK Centralized Logging](./elk/README.md)
- [Architecture Documentation](./MICROSERVICES_ARCHITECTURE.md)

---

## Contribución | Contributing

Este es un proyecto de portafolio personal. Sugerencias y feedback son bienvenidos.

---

## Licencia | License

MIT License - ver [LICENSE](LICENSE) para más detalles.

---

## Autor | Author

**Cristian A. Soncco Boza** - Tech Lead & Full Stack Developer

---

## Enlaces | Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

---

**Si te gusta este proyecto, dale una estrella! | If you like this project, give it a star!**
