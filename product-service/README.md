# Product Service | Servicio de Productos

Product catalog management service for MicroCommerce platform.  
Servicio de gestión de catálogo de productos para la plataforma MicroCommerce.

---

## Description | Descripción

**EN:** Product Service manages the product catalog for the e-commerce platform. It provides CRUD operations for products, search capabilities, stock management, and will integrate with Redis for caching (coming in next commit).

**ES:** Product Service gestiona el catálogo de productos para la plataforma de e-commerce. Proporciona operaciones CRUD para productos, capacidades de búsqueda, gestión de inventario, y se integrará con Redis para caché (próximo commit).

---

## Technology Stack | Stack Tecnológico

- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL 16
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Lombok
- Java 17

---

## Configuration | Configuración

- **Port | Puerto**: 8081
- **Database | Base de Datos**: PostgreSQL (productdb)
- **Eureka Registration | Registro en Eureka**: Yes | Sí
- **Config Server**: Yes | Sí

---

## Database Schema | Esquema de Base de Datos

### Products Table | Tabla Products

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

---

## Entity | Entidad

### Product

**EN:** Represents a product in the catalog.

**ES:** Representa un producto en el catálogo.

**Fields | Campos:**
- `id` - Unique identifier | Identificador único
- `name` - Product name | Nombre del producto
- `description` - Product description | Descripción del producto
- `price` - Product price | Precio del producto
- `stock` - Available quantity | Cantidad disponible
- `category` - Product category | Categoría del producto
- `imageUrl` - Product image URL | URL de imagen del producto
- `active` - Active status | Estado activo
- `createdAt` - Creation timestamp | Fecha de creación
- `updatedAt` - Last update timestamp | Fecha de última actualización

---

## Repository Capabilities | Capacidades del Repositorio

### Standard CRUD | CRUD Estándar
- `save()` - Create or update product
- `findById()` - Find product by ID
- `findAll()` - List all products
- `deleteById()` - Delete product

### Custom Queries | Queries Personalizadas

**EN:**
- `findByCategory()` - Find products by category
- `findByCategoryAndActiveTrue()` - Find active products by category
- `findByPriceBetween()` - Find products within price range
- `findByNameContainingIgnoreCase()` - Search products by name
- `findAvailableProducts()` - Find active products with stock
- `findActiveByCategoryOrderByNewest()` - Find newest products by category
- `existsByName()` - Check if product name exists
- `countByCategory()` - Count products by category
- `findLowStockProducts()` - Find products with low stock

**ES:**
- `findByCategory()` - Buscar productos por categoría
- `findByCategoryAndActiveTrue()` - Buscar productos activos por categoría
- `findByPriceBetween()` - Buscar productos en rango de precio
- `findByNameContainingIgnoreCase()` - Buscar productos por nombre
- `findAvailableProducts()` - Buscar productos activos con stock
- `findActiveByCategoryOrderByNewest()` - Buscar productos más nuevos por categoría
- `existsByName()` - Verificar si existe nombre de producto
- `countByCategory()` - Contar productos por categoría
- `findLowStockProducts()` - Buscar productos con stock bajo

---

## Prerequisites | Prerequisitos

### PostgreSQL Setup | Configuración de PostgreSQL

**Option 1: Using Docker | Opción 1: Usando Docker (Recomendado)**

```bash
docker run --name postgres-product \
  -e POSTGRES_DB=productdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

**Option 2: Local Installation | Opción 2: Instalación Local**

1. Install PostgreSQL 16
2. Create database:
```sql
CREATE DATABASE productdb;
```

---

## Running Locally | Ejecución Local

### Prerequisites | Prerequisitos
- Java 17+
- Maven 3.9+
- PostgreSQL running on port 5432
- Eureka Server running on port 8761
- Config Server running on port 8888 (optional)

### Run with Maven | Ejecutar con Maven

```bash
cd product-service
mvn spring-boot:run
```

---

## Verification | Verificación

### Check Service Health | Verificar Salud del Servicio

```bash
curl http://localhost:8081/actuator/health
```

**Expected response | Respuesta esperada:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "eureka": {
      "status": "UP"
    }
  }
}
```

### Check Eureka Registration | Verificar Registro en Eureka

**EN:** Open Eureka Dashboard and verify `PRODUCT-SERVICE` is registered:

**ES:** Abre el Dashboard de Eureka y verifica que `PRODUCT-SERVICE` está registrado:

```
http://localhost:8761
```

---

## Current Implementation Status | Estado Actual de Implementación

###  Implemented | Implementado

- Product entity with JPA annotations
- ProductRepository with custom queries
- ProductDTO for data transfer
- PostgreSQL datasource configuration
- Eureka Client integration
- Config Client integration
- Health checks with Actuator
- Hibernate schema auto-generation

###  Coming in Next Commits | Próximos Commits

- **Commit 6**: Service layer with business logic
- **Commit 6**: Redis caching integration
- **Commit 7**: REST API Controller
- **Commit 7**: Request validation
- **Commit 7**: Error handling
- **Commit 8**: Comprehensive test suite
- **Commit 8**: Integration tests with TestContainers

---

## DTO Validation | Validación de DTO

**EN:** ProductDTO includes validation constraints:

**ES:** ProductDTO incluye restricciones de validación:

- `name`: Required, 3-200 characters
- `description`: Optional, max 1000 characters
- `price`: Required, 0.01 - 999,999.99
- `stock`: Required, >= 0
- `category`: Required, max 100 characters
- `imageUrl`: Optional, max 500 characters
- `active`: Optional, defaults to true

---

## Database Connection Pool | Pool de Conexiones

**Configuration | Configuración:**
- Maximum pool size: 10 connections
- Minimum idle: 5 connections
- Connection timeout: 20 seconds

---

## Logging | Registro de Logs

**EN:** Detailed logging is enabled for:

**ES:** Registro detallado está habilitado para:

- Application code: `DEBUG`
- Hibernate SQL: `DEBUG`
- SQL parameter binding: `TRACE`

---

## Environment Variables | Variables de Entorno

**EN:** You can override configuration with environment variables:

**ES:** Puedes sobrescribir configuración con variables de entorno:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/productdb
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=prod_password
export EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-prod:8761/eureka/
```

---

## Notes | Notas

**EN:**
- Schema is automatically created/updated by Hibernate (`ddl-auto: update`)
- Service registers with Eureka on startup
- Configuration can be centralized via Config Server
- Lombok is used to reduce boilerplate code
- Entity uses optimistic locking (implicit via `@Version` if needed)

**ES:**
- Esquema se crea/actualiza automáticamente por Hibernate (`ddl-auto: update`)
- Servicio se registra en Eureka al iniciar
- Configuración puede centralizarse vía Config Server
- Lombok se usa para reducir código repetitivo
- Entidad usa bloqueo optimista (implícito vía `@Version` si es necesario)

---

## Troubleshooting | Resolución de Problemas

### Cannot Connect to PostgreSQL | No Puede Conectar a PostgreSQL

**EN:** Ensure PostgreSQL is running:

**ES:** Asegúrate que PostgreSQL está corriendo:

```bash
# Check if PostgreSQL is running
docker ps | grep postgres-product

# View PostgreSQL logs
docker logs postgres-product
```

### Service Not Registering with Eureka | Servicio No Se Registra en Eureka

**EN:** Verify Eureka Server is accessible:

**ES:** Verifica que Eureka Server es accesible:

```bash
curl http://localhost:8761/eureka/apps
```

---

## References | Referencias

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Hibernate ORM](https://hibernate.org/)
- [Lombok Project](https://projectlombok.org/)

