# API Gateway | Puerta de Enlace API

Single entry point for all client requests in MicroCommerce.  
Punto de entrada unico para todas las peticiones cliente en MicroCommerce.

---

## Descripcion | Description

**ES:** El API Gateway centraliza el acceso a los microservicios del sistema. Se encarga del enrutamiento, del descubrimiento de servicios via Eureka, de la validacion de acceso a rutas protegidas y de aplicar circuit breakers para tolerancia a fallos.

**EN:** The API Gateway centralizes access to system microservices. It handles routing, service discovery through Eureka, access validation for protected routes, and circuit breakers for fault tolerance.

---

## Stack Tecnologico | Technology Stack

- Spring Boot 3.2
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config
- Resilience4j Circuit Breaker
- JJWT
- Java 17

---

## Configuracion General | General Configuration

- **Puerto | Port:** `8080`
- **Registro en Eureka | Eureka Registration:** `Yes`
- **Config Server:** `Yes`
- **Health Check:** `http://localhost:8080/actuator/health`
- **Actuator Routes:** `http://localhost:8080/actuator/gateway/routes`

---

## Responsabilidades | Responsibilities

### 1. Enrutamiento | Routing

**ES:** Reenvia peticiones a los microservicios correspondientes segun el path solicitado.

**EN:** Forwards requests to the corresponding microservice based on the requested path.

### 2. Descubrimiento de Servicios | Service Discovery

**ES:** Resuelve destinos con `lb://...` usando Eureka en lugar de URLs hardcodeadas.

**EN:** Resolves `lb://...` destinations through Eureka instead of hardcoded URLs.

### 3. Seguridad de Acceso | Access Security

**ES:** Diferencia rutas publicas y protegidas. En las protegidas exige un JWT valido antes de reenviar la peticion.

**EN:** Distinguishes public and protected routes. For protected routes it requires a valid JWT before forwarding the request.

### 4. Resiliencia | Resilience

**ES:** Aplica circuit breakers para responder de forma controlada cuando un servicio aguas abajo falla.

**EN:** Applies circuit breakers to respond in a controlled way when a downstream service fails.

---

## Rutas Configuradas | Configured Routes

| Ruta Externa | Servicio Destino | Acceso | Descripcion |
|---|---|---|---|
| `/api/products/**` | `product-service` | Publico solo para `GET` | Catalogo |
| `/api/auth/**` | `user-service` | Publico | Registro y login |
| `/api/users/**` | `user-service` | Protegido | Gestion de usuarios |
| `/api/orders/**` | `order-service` | Protegido | Gestion de pedidos |
| `/api/payments/**` | `payment-service` | Protegido | Gestion de pagos |

**Importante | Important**

- El gateway conserva el prefijo `/api/...`
- No usa `StripPrefix` en estas rutas

Ejemplo real:

```text
Cliente:  GET http://localhost:8080/api/products/123
Gateway:  ->  http://product-service:8081/api/products/123
```

---

## Reglas de Acceso | Access Rules

### Endpoints Publicos | Public Endpoints

- `/api/auth/**`
- `GET /api/products/**`

### Endpoints Protegidos | Protected Endpoints

- `POST`, `PUT`, `PATCH`, `DELETE` sobre `/api/products/**`
- `/api/users/**`
- `/api/orders/**`
- `/api/payments/**`

---

## Validacion JWT | JWT Validation

**ES:** En rutas protegidas el gateway valida el JWT antes de reenviar la peticion.

**EN:** On protected routes the gateway validates the JWT before forwarding the request.

Comprobaciones actuales:

1. existe cabecera `Authorization`
2. empieza por `Bearer `
3. la firma del JWT es valida
4. el token no esta expirado

Resultado esperado:

- token valido -> `200` o la respuesta funcional del servicio destino
- token invalido o mal formado -> `401 Unauthorized`

---

## Circuit Breaker

Cada ruta protegida por backend usa Resilience4j con fallback.

Configuracion base actual:

- `sliding-window-size`: `10`
- `failure-rate-threshold`: `50`
- `wait-duration-in-open-state`: `10000`

Servicios cubiertos:

- `product-service`
- `order-service`
- `user-service`
- `payment-service`

---

## Ejecucion Local | Running Locally

### Prerrequisitos | Prerequisites

- Java 17+
- Maven 3.9+
- Eureka Server en `8761`
- Config Server en `8888`

### Maven

```bash
cd api-gateway
mvn spring-boot:run
```

### Docker

```bash
cd api-gateway
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

---

## Pruebas Recomendadas | Recommended Checks

### 1. Health

```bash
curl http://localhost:8080/actuator/health
```

### 2. Rutas del Gateway

```bash
curl http://localhost:8080/actuator/gateway/routes
```

### 3. Ruta Publica de Productos

```bash
curl http://localhost:8080/api/products
```

Esperado: `200`

### 4. Ruta Protegida sin Token

```bash
curl http://localhost:8080/api/orders
```

Esperado: `401`

### 5. Login Publico

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}'
```

### 6. Ruta Protegida con JWT Valido

```bash
curl -H "Authorization: Bearer <valid-jwt>" \
  http://localhost:8080/api/orders
```

Esperado: `200`

### 7. Ruta Protegida con JWT Invalido

```bash
curl -H "Authorization: Bearer token-invalido" \
  http://localhost:8080/api/orders
```

Esperado: `401`

---

## Flujo de Peticion | Request Flow

1. El cliente llama a `http://localhost:8080/api/...`
2. El gateway decide si la ruta es publica o protegida
3. Si es protegida, valida el JWT
4. Consulta Eureka para resolver el servicio destino
5. Aplica filtros del gateway y circuit breaker
6. Reenvia la peticion al microservicio correspondiente
7. Devuelve la respuesta al cliente

---

## Archivos de Configuracion | Configuration Files

### Configuracion Local

- `api-gateway/src/main/resources/application.yml`

### Configuracion Centralizada

- `config/api-gateway.yml`

Ambas deben mantenerse alineadas en:

- rutas configuradas
- reglas publicas/protegidas
- exposicion de actuator
- `jwt.secret`

---

## Respuestas de Error | Error Responses

### 401 Unauthorized

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing, malformed or invalid JWT token"
}
```

### 503 Service Unavailable

```json
{
  "message": "Product Service is currently unavailable. Please try again later.",
  "status": "SERVICE_UNAVAILABLE"
}
```

---

## Monitoreo | Monitoring

Endpoints utiles:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/gateway/routes`

---

## Troubleshooting

### El gateway arranca pero no enruta

Revisar:

1. Eureka Server esta arriba
2. El servicio destino esta registrado
3. Config Server esta sirviendo `api-gateway.yml`
4. La ruta esperada conserva `/api/...`

### Una ruta protegida acepta token invalido

Revisar:

1. `jwt.secret` coincide con `user-service`
2. `AuthenticationFilter` llama a `JwtTokenValidator`
3. Se reinicio el gateway tras cambios de codigo o config

---

## Referencias | References

- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
