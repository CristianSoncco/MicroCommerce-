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
- Redis 7 (Caching)
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Lombok
- Java 17

---

## Configuration | Configuración

- **Port | Puerto**: 8081
- **Database | Base de Datos**: PostgreSQL (productdb)
- **Cache | Caché**: Redis (localhost:6379)
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

### Docker Compose (Recommended | Recomendado)

**EN:** Use Docker Compose to start PostgreSQL + Redis together:

**ES:** Usa Docker Compose para iniciar PostgreSQL + Redis juntos:

```bash
# From project root | Desde la raíz del proyecto
docker-compose -f docker-compose-dev.yml up -d

# Verify services | Verificar servicios
docker-compose -f docker-compose-dev.yml ps

# Stop services | Detener servicios
docker-compose -f docker-compose-dev.yml down
```

### PostgreSQL Setup (Manual) | Configuración de PostgreSQL (Manual)

**Option 1: Using Docker | Opción 1: Usando Docker**

```bash
docker run --name postgres-product \
  -e POSTGRES_DB=productdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16-alpine
```

**Option 2: Local Installation | Opción 2: Instalación Local**

1. Install PostgreSQL 16
2. Create database:
```sql
CREATE DATABASE productdb;
```

### Redis Setup (Manual) | Configuración de Redis (Manual)

**Using Docker | Usando Docker:**

```bash
# Start Redis
docker run -d \
  --name redis-product \
  -p 6379:6379 \
  redis:7-alpine redis-server --appendonly yes

# Test Redis connection | Probar conexión Redis
docker exec redis-product redis-cli ping
# Should return: PONG

# View Redis data | Ver datos en Redis
docker exec -it redis-product redis-cli
> KEYS product:*
> GET product:1
```

---

## Running Locally | Ejecución Local

### Prerequisites | Prerequisitos
- Java 17+
- Maven 3.9+
- PostgreSQL running on port 5432
- Redis running on port 6379
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
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### Test API Endpoints | Probar Endpoints de la API

#### Using Swagger UI | Usando Swagger UI
**EN:** Open Swagger UI for interactive API testing:

**ES:** Abre Swagger UI para pruebas interactivas de la API:

```
http://localhost:8081/swagger-ui.html
```

#### Using cURL | Usando cURL

**Create a product | Crear un producto:**
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "description": "Product for testing",
    "price": 99.99,
    "stock": 100,
    "category": "Test",
    "active": true
  }'
```

**Get all products | Obtener todos los productos:**
```bash
curl http://localhost:8081/api/products
```

### Check Eureka Registration | Verificar Registro en Eureka

**EN:** Open Eureka Dashboard and verify `PRODUCT-SERVICE` is registered:

**ES:** Abre el Dashboard de Eureka y verifica que `PRODUCT-SERVICE` está registrado:

```
http://localhost:8761
```

---

## Current Implementation Status | Estado Actual de Implementación

### Implemented | Implementado

- Product entity with JPA annotations
- ProductRepository with custom queries
- ProductDTO for data transfer with validation
- PostgreSQL datasource configuration
- **ProductService with business logic** (Commit 6)
- **Redis caching with Cache-Aside pattern** (Commit 6)
- **Stock management (increase/decrease)** (Commit 6)
- **Custom exceptions** (ProductNotFoundException, InsufficientStockException, ProductAlreadyExistsException)
- **ProductMapper for Entity-DTO conversion** (Commit 6)
- **ProductController with 13 REST endpoints** (Commit 7)
- **Global Exception Handler** (Commit 7)
- **Swagger/OpenAPI documentation** (Commit 7)
- **Request/Response DTOs** (Commit 7)
- **Comprehensive test suite** (Commit 8):
  - 22 unit tests for ProductService with Mockito
  - 13 unit tests for ProductMapper
  - 14 integration tests for ProductRepository with TestContainers
  - 20 controller tests with MockMvc
  - Code coverage >80% with JaCoCo
- Eureka Client integration
- Config Client integration
- Health checks with Actuator
- Hibernate schema auto-generation

### Coming in Next Commits | Próximos Commits

- Order Service implementation (Commits 9-12)
- User Service with JWT authentication (Commits 13-16)
- Payment Service integration (Commits 17-20)

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

## Redis Caching Strategy | Estrategia de Caché Redis

### Cache-Aside Pattern | Patrón Cache-Aside

**EN:**
- **Read**: Check cache first, if miss → read from DB → cache result
- **Write**: Update DB → invalidate cache
- **TTL**: 1 hour (configurable)
- **Keys**: `product:{id}`

**ES:**
- **Lectura**: Verifica caché primero, si falla → lee de BD → cachea resultado
- **Escritura**: Actualiza BD → invalida caché
- **TTL**: 1 hora (configurable)
- **Claves**: `product:{id}`

### Cache Operations | Operaciones de Caché

**EN:**
- `getProductById()` → Cache read
- `createProduct()` → Cache write
- `updateProduct()` → Cache invalidation + write
- `deleteProduct()` → Cache invalidation
- `decreaseStock()` / `increaseStock()` → Cache invalidation

**ES:**
- `getProductById()` → Lectura de caché
- `createProduct()` → Escritura en caché
- `updateProduct()` → Invalidación + escritura de caché
- `deleteProduct()` → Invalidación de caché
- `decreaseStock()` / `increaseStock()` → Invalidación de caché

---

## REST API Endpoints | Endpoints de la API REST

### Base URL
- **Local**: `http://localhost:8081/api/products`
- **Via Gateway**: `http://localhost:8080/api/products`

### Swagger UI | Interfaz Swagger
- **URL**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8081/api-docs`

### Available Endpoints | Endpoints Disponibles

#### CRUD Operations | Operaciones CRUD
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Crear producto \| Create product |
| GET | `/api/products/{id}` | Obtener producto por ID \| Get product by ID |
| GET | `/api/products` | Listar todos \| List all products |
| GET | `/api/products/active` | Listar activos \| List active products |
| PUT | `/api/products/{id}` | Actualizar \| Update product |
| DELETE | `/api/products/{id}` | Eliminar \| Delete product |

#### Search Operations | Operaciones de Búsqueda
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/search?name={name}` | Buscar por nombre \| Search by name |
| GET | `/api/products/category/{category}` | Buscar por categoría \| Search by category |
| GET | `/api/products/price-range?minPrice={min}&maxPrice={max}` | Buscar por rango de precio \| Search by price range |
| GET | `/api/products/low-stock?threshold={threshold}` | Productos con stock bajo \| Low stock products |

#### Stock Management | Gestión de Stock
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/{id}/check-stock?quantity={quantity}` | Verificar stock \| Check stock |
| PATCH | `/api/products/{id}/decrease-stock?quantity={quantity}` | Disminuir stock \| Decrease stock |
| PATCH | `/api/products/{id}/increase-stock?quantity={quantity}` | Aumentar stock \| Increase stock |

### Example Requests | Ejemplos de Peticiones

#### Create Product | Crear Producto
```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop HP",
    "description": "Laptop HP 15 pulgadas",
    "price": 899.99,
    "stock": 50,
    "category": "Electronics",
    "imageUrl": "https://example.com/laptop.jpg",
    "active": true
  }'
```

#### Get Product | Obtener Producto
```bash
curl http://localhost:8081/api/products/1
```

#### Search by Category | Buscar por Categoría
```bash
curl http://localhost:8081/api/products/category/Electronics
```

#### Update Stock | Actualizar Stock
```bash
curl -X PATCH "http://localhost:8081/api/products/1/decrease-stock?quantity=5"
```

---

## Service Layer | Capa de Servicio

### ProductService Methods | Métodos de ProductService

**CRUD Operations | Operaciones CRUD:**
- `createProduct(dto)` - Create new product
- `getProductById(id)` - Get product (cached)
- `getAllProducts()` - Get all products
- `getActiveProducts()` - Get active products
- `updateProduct(id, dto)` - Update product
- `deleteProduct(id)` - Delete product

**Search Operations | Operaciones de Búsqueda:**
- `searchByName(name)` - Search by name
- `searchByCategory(category)` - Search by category
- `searchByPriceRange(min, max)` - Search by price range
- `getAvailableProducts()` - Get available products

**Stock Management | Gestión de Stock:**
- `checkStock(productId, quantity)` - Check stock availability
- `decreaseStock(productId, quantity)` - Decrease stock
- `increaseStock(productId, quantity)` - Increase stock
- `getLowStockProducts(threshold)` - Get low stock products

**Statistics | Estadísticas:**
- `countByCategory(category)` - Count products by category

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

### Cannot Connect to Redis | No Puede Conectar a Redis

**EN:** Ensure Redis is running:

**ES:** Asegúrate que Redis está corriendo:

```bash
# Check if Redis is running
docker ps | grep redis-product

# View Redis logs
docker logs redis-product

# Test Redis connection
docker exec redis-product redis-cli ping
# Expected: PONG
```

### Service Not Registering with Eureka | Servicio No Se Registra en Eureka

**EN:** Verify Eureka Server is accessible:

**ES:** Verifica que Eureka Server es accesible:

```bash
curl http://localhost:8761/eureka/apps
```

### Redis Connection Timeout | Tiempo de Espera Redis Agotado

**EN:** Check Redis configuration in `application.yml`:

**ES:** Verifica configuración Redis en `application.yml`:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
```

---

## References | Referencias

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
- [Hibernate ORM](https://hibernate.org/)
- [Lombok Project](https://projectlombok.org/)

