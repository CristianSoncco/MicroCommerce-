# API Gateway | Puerta de Enlace API

Single entry point for all microservices in MicroCommerce architecture.  
Punto de entrada único para todos los microservicios en la arquitectura de MicroCommerce.

---

## Description | Descripción

**EN:** API Gateway acts as the single entry point for all client requests. It routes requests to appropriate microservices, handles authentication, implements circuit breakers for fault tolerance, and provides centralized CORS configuration.

**ES:** API Gateway actúa como el punto de entrada único para todas las peticiones de clientes. Enruta peticiones a los microservicios apropiados, maneja autenticación, implementa circuit breakers para tolerancia a fallos, y proporciona configuración CORS centralizada.

---

## Technology Stack | Stack Tecnológico

- Spring Boot 3.2
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Resilience4j (Circuit Breaker)
- Java 17

---

## Configuration | Configuración

- **Port | Puerto**: 8080
- **Eureka Registration | Registro en Eureka**: Yes | Sí
- **Config Server**: Yes | Sí
- **Actuator Health Check**: http://localhost:8080/actuator/health

---

## Routes | Rutas

### Configured Routes | Rutas Configuradas

| External Path | Internal Service | Auth Required | Description |
|--------------|------------------|---------------|-------------|
| `/api/products/**` | `product-service` | No (GET only) | Product catalog |
| `/api/orders/**` | `order-service` | Yes | Order management |
| `/api/users/**` | `user-service` | Partial | User management |
| `/api/payments/**` | `payment-service` | Yes | Payment processing |

**EN:** All routes use load balancing via Eureka.

**ES:** Todas las rutas usan balanceo de carga vía Eureka.

---

## Features | Características

### 1. **Routing | Enrutamiento**

**EN:** Automatically routes requests based on path patterns to corresponding microservices.

**ES:** Enruta automáticamente peticiones basado en patrones de ruta a los microservicios correspondientes.

```
Client Request: GET http://localhost:8080/api/products/123
                    ↓
API Gateway:      Routes to → http://product-service:8081/products/123
```

### 2. **Authentication Filter | Filtro de Autenticación**

**EN:** Validates Bearer tokens for protected endpoints.

**ES:** Valida tokens Bearer para endpoints protegidos.

**Public endpoints | Endpoints públicos:**
- `/api/users/register`
- `/api/users/login`
- `/api/products` (GET only)

**Protected endpoints | Endpoints protegidos:**
- All others require `Authorization: Bearer <token>` header

### 3. **Circuit Breaker | Disyuntor**

**EN:** Implements circuit breaker pattern to handle service failures gracefully.

**ES:** Implementa patrón circuit breaker para manejar fallos de servicios elegantemente.

**Configuration:**
- **Sliding Window**: 10 requests
- **Failure Threshold**: 50%
- **Wait Duration**: 10 seconds

### 4. **CORS Configuration | Configuración CORS**

**EN:** Centralized CORS configuration for frontend applications.

**ES:** Configuración CORS centralizada para aplicaciones frontend.

**Allowed origins | Orígenes permitidos:**
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)

---

## Running Locally | Ejecución Local

### Prerequisites | Prerequisitos
- Java 17+
- Maven 3.9+
- Eureka Server running on port 8761
- Config Server running on port 8888

### Run with Maven | Ejecutar con Maven

```bash
cd api-gateway
mvn spring-boot:run
```

### Run with Docker | Ejecutar con Docker

```bash
cd api-gateway
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

---

## Testing | Pruebas

### Test Routing | Probar Enrutamiento

```bash
# Test product service route (public)
curl http://localhost:8080/api/products

# Test with authentication (should fail without token)
curl http://localhost:8080/api/orders
# Expected: 401 Unauthorized

# Test with Bearer token
curl -H "Authorization: Bearer your-token-here" \
     http://localhost:8080/api/orders
```

### Test Circuit Breaker | Probar Circuit Breaker

**EN:** Stop a microservice and observe fallback responses.

**ES:** Detén un microservicio y observa respuestas de fallback.

```bash
# Stop product-service, then:
curl http://localhost:8080/api/products

# Expected response:
{
  "message": "Product Service is currently unavailable. Please try again later.",
  "status": "SERVICE_UNAVAILABLE"
}
```

---

## Health Check | Verificación de Salud

```bash
curl http://localhost:8080/actuator/health
```

**Expected response | Respuesta esperada:**

```json
{
  "status": "UP",
  "components": {
    "eureka": {
      "status": "UP"
    },
    "gateway": {
      "status": "UP"
    }
  }
}
```

---

## Gateway Actuator Endpoints | Endpoints de Actuator del Gateway

### View Routes | Ver Rutas

```bash
curl http://localhost:8080/actuator/gateway/routes
```

**EN:** Returns all configured routes with their predicates and filters.

**ES:** Retorna todas las rutas configuradas con sus predicados y filtros.

### Refresh Routes | Refrescar Rutas

```bash
curl -X POST http://localhost:8080/actuator/gateway/refresh
```

---

## Request Flow | Flujo de Petición

```
┌────────────┐
│   Client   │
└─────┬──────┘
      │ 1. HTTP Request
      │ GET /api/products/123
      ▼
┌──────────────────────┐
│    API Gateway       │
│    Port 8080         │
└──────┬───────────────┘
       │ 2. Authentication Check
       │ 3. Query Eureka for service
       │ 4. Apply Circuit Breaker
       ▼
┌──────────────────────┐
│  Product Service     │
│  (via Eureka)        │
└──────┬───────────────┘
       │ 5. Process request
       │ 6. Return response
       ▼
┌──────────────────────┐
│    API Gateway       │
│  (returns to client) │
└──────────────────────┘
```

---

## Authentication Implementation | Implementación de Autenticación

### Current (Phase 1) | Actual (Fase 1)

**EN:** Validates Bearer token format only.

**ES:** Valida solo el formato del token Bearer.

```java
// Checks:
// 1. Authorization header exists
// 2. Header starts with "Bearer "
```

### Future (Phase 2) | Futuro (Fase 2)

**EN:** When User Service with JWT is implemented:

**ES:** Cuando User Service con JWT esté implementado:

```java
// Will add:
// 3. Validate JWT signature
// 4. Check token expiration
// 5. Extract user roles
```

---

## Configuration Files | Archivos de Configuración

### Local Configuration | Configuración Local
- `api-gateway/src/main/resources/application.yml`

### Centralized Configuration | Configuración Centralizada
- `config/api-gateway.yml` (served by Config Server)

---

## Error Responses | Respuestas de Error

### 401 Unauthorized

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid Authorization header"
}
```

### 503 Service Unavailable (Circuit Breaker Open)

```json
{
  "message": "Product Service is currently unavailable. Please try again later.",
  "status": "SERVICE_UNAVAILABLE"
}
```

### 404 Not Found

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "No route found for request"
}
```

---

## Monitoring | Monitoreo

**EN:** Available actuator endpoints:

**ES:** Endpoints de actuator disponibles:

- `/actuator/health` - Health status | Estado de salud
- `/actuator/info` - Application info | Información de la aplicación
- `/actuator/metrics` - Metrics data | Datos de métricas
- `/actuator/gateway/routes` - Configured routes | Rutas configuradas

---

## Docker Build

**EN:** Multi-stage build optimizes image size:

**ES:** La construcción multi-etapa optimiza el tamaño de la imagen:

- **Build stage | Etapa de construcción**: Uses JDK to compile | Usa JDK para compilar
- **Runtime stage | Etapa de ejecución**: Uses lightweight JRE (Alpine-based) | Usa JRE ligero (basado en Alpine)

---

## Troubleshooting | Resolución de Problemas

### Gateway Can't Find Services | Gateway No Encuentra Servicios

**EN:** Ensure:
1. Eureka Server is running
2. Target services are registered in Eureka
3. Check logs: `logging.level.org.springframework.cloud.gateway: DEBUG`

**ES:** Asegúrate:
1. Eureka Server está corriendo
2. Servicios objetivo están registrados en Eureka
3. Revisa logs: `logging.level.org.springframework.cloud.gateway: DEBUG`

### Circuit Breaker Not Working | Circuit Breaker No Funciona

**EN:** Verify:
1. Resilience4j dependency is included
2. Circuit breaker configuration is correct
3. Fallback controller is implemented

**ES:** Verifica:
1. Dependencia Resilience4j está incluida
2. Configuración de circuit breaker es correcta
3. Controlador de fallback está implementado

---

## Notes | Notas

**EN:**
- Gateway registers with Eureka for service discovery
- Uses reactive programming model (Spring WebFlux)
- Load balancing is handled automatically via Eureka
- Circuit breakers prevent cascading failures
- CORS is configured for common frontend frameworks

**ES:**
- Gateway se registra en Eureka para descubrimiento de servicios
- Usa modelo de programación reactiva (Spring WebFlux)
- Balanceo de carga se maneja automáticamente vía Eureka
- Circuit breakers previenen fallos en cascada
- CORS está configurado para frameworks frontend comunes

---

## References | Referencias

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)

