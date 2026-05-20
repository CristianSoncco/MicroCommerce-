# Payment Service | Servicio de Pagos

Payment processing service for the MicroCommerce platform.  
Servicio de procesamiento de pagos para la plataforma MicroCommerce.

---

## Description | Descripcion

**EN:** Payment Service is responsible for creating payments, querying their status, processing refunds, cancelling pending payments, and publishing payment events for the rest of the platform.

**ES:** Payment Service es responsable de crear pagos, consultar su estado, procesar reembolsos, cancelar pagos pendientes y publicar eventos de pago para el resto de la plataforma.

---

## Technology Stack | Stack Tecnologico

- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL 16
- RabbitMQ 3.13
- Spring Cloud Netflix Eureka Client
- Spring Cloud Config Client
- Spring WebFlux WebClient
- Resilience4j
- Stripe API
- Lombok
- Java 17

---

## Configuration | Configuracion

- **Port | Puerto**: `8084`
- **Database | Base de Datos**: PostgreSQL (`paymentdb`)
- **RabbitMQ**: `localhost:5672`
- **Eureka Registration | Registro en Eureka**: `Yes`
- **Config Server**: `Yes`
- **Health Check**: `http://localhost:8084/actuator/health`

### Stripe

- `stripe.api.key`
- `stripe.api.base-url`
- `stripe.webhook.secret`

**EN:** For local development, the project uses test placeholders by default.  
**ES:** Para desarrollo local, el proyecto usa placeholders de prueba por defecto.

---

## Functional Scope | Alcance Funcional

### Implemented Responsibilities | Responsabilidades Implementadas

- Process a new payment
- Query payments by internal ID
- Query payments by Stripe PaymentIntent ID
- Query payments by order, user, and status
- Refund completed payments
- Cancel pending payments
- Publish `PAYMENT_COMPLETED`, `PAYMENT_FAILED`, and `PAYMENT_REFUNDED` events

### Important Contract Rules | Reglas Importantes del Contrato

#### `orderId`

**EN:** `payment-service` uses `orderId` as `String` to stay aligned with `order-service`, which persists orders in MongoDB using string document IDs.

**ES:** `payment-service` usa `orderId` como `String` para mantenerse alineado con `order-service`, que persiste pedidos en MongoDB usando IDs de documento tipo string.

#### `paymentToken`

**EN:** Creating a payment requires `paymentToken`. Without it, the request is invalid.

**ES:** La creacion de un pago requiere `paymentToken`. Sin ese campo, la peticion es invalida.

---

## Data Model | Modelo de Datos

### Payment

**Fields | Campos:**

- `id` - Internal payment identifier | Identificador interno del pago
- `orderId` - Related order ID (`String`) | ID del pedido relacionado (`String`)
- `userId` - User ID | ID de usuario
- `amount` - Payment amount | Monto del pago
- `currency` - Currency code | Codigo de moneda
- `status` - Payment status | Estado del pago
- `paymentMethod` - Payment method | Metodo de pago
- `stripePaymentIntentId` - Stripe PaymentIntent ID
- `stripeChargeId` - Stripe charge ID
- `failureReason` - Failure reason when payment fails | Motivo de fallo cuando el pago falla
- `description` - Business description | Descripcion funcional
- `createdAt` - Creation timestamp | Fecha de creacion
- `updatedAt` - Last update timestamp | Fecha de ultima actualizacion

### Payment Status | Estado del Pago

- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`
- `REFUNDED`
- `CANCELLED`

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/payments` | List all payments |
| `POST` | `/api/payments` | Process a new payment |
| `GET` | `/api/payments/{id}` | Get payment by internal ID |
| `GET` | `/api/payments/stripe/{stripePaymentIntentId}` | Get payment by Stripe PaymentIntent ID |
| `GET` | `/api/payments/order/{orderId}` | Get payments by order |
| `GET` | `/api/payments/user/{userId}` | Get payments by user |
| `GET` | `/api/payments/status/{status}` | Get payments by status |
| `GET` | `/api/payments/user/{userId}/status/{status}` | Get payments by user and status |
| `POST` | `/api/payments/{id}/refund` | Refund a completed payment |
| `POST` | `/api/payments/{id}/cancel` | Cancel a pending payment |

---

## Request Examples | Ejemplos de Peticion

### Create Payment | Crear Pago

```json
{
  "orderId": "6a0c56d54ff9423cea4576c9",
  "userId": 1,
  "amount": 1799.98,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "description": "Pago del pedido 6a0c56d54ff9423cea4576c9",
  "paymentToken": "pm_test_visa_001"
}
```

### Refund Payment | Reembolsar Pago

```json
{
  "amount": 100.00,
  "reason": "Customer requested refund"
}
```

---

## Running Locally | Ejecucion Local

### Prerequisites | Prerrequisitos

- Java 17+
- Maven 3.9+
- PostgreSQL running on `5434`
- RabbitMQ running on `5672`
- Eureka Server running on `8761`
- Config Server running on `8888`

### Maven

```bash
cd payment-service
mvn spring-boot:run
```

---

## Verification | Verificacion

### Health Check

```bash
curl http://localhost:8084/actuator/health
```

### List Payments

```bash
curl http://localhost:8084/api/payments
```

### Get Payments by Order

```bash
curl http://localhost:8084/api/payments/order/6a0c56d54ff9423cea4576c9
```

### Process Payment

```bash
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "6a0c56d54ff9423cea4576c9",
    "userId": 1,
    "amount": 1799.98,
    "currency": "USD",
    "paymentMethod": "CREDIT_CARD",
    "description": "Pago de prueba",
    "paymentToken": "pm_test_visa_001"
  }'
```

---

## Integration Notes | Notas de Integracion

### Order Service

- `order-service` and `payment-service` must share the same `orderId` value
- that identifier is a `String`
- payment events must reference the same `orderId` that exists in MongoDB orders

### RabbitMQ

Published routing keys:

- `payment.completed`
- `payment.failed`
- `payment.refunded`

**EN:** `order-service` can consume `payment.#` and react to successful or failed payments.  
**ES:** `order-service` puede consumir `payment.#` y reaccionar a pagos exitosos o fallidos.

### Stripe

**EN:** Payment processing depends on Stripe integration or test credentials.  
**ES:** El procesamiento de pagos depende de la integracion con Stripe o de credenciales de prueba.

---

## Testing | Pruebas

### Unit Tests

```bash
mvn -pl payment-service -Dtest="*Test,!*IntegrationTest" test
```

### Integration Tests

```bash
mvn -pl payment-service test
```

**Nota:** En Windows, los tests con Testcontainers pueden depender de que Docker/JNA tenga permisos correctos en el entorno local.

---

## Troubleshooting

### `GET /api/payments` returns `500`

Check:

1. PostgreSQL is running on `5434`
2. `payment-service` can connect to `paymentdb`
3. the table schema was created correctly

### Creating payment fails unexpectedly

Check:

1. `paymentToken` is present
2. `orderId` is sent as `String`
3. Stripe configuration is available for the current environment

### Payment and order do not correlate

Check:

1. the `orderId` used in payment exactly matches the MongoDB order ID
2. payment events publish the same `orderId`
3. `order-service` is consuming `payment.#`

---

## Operational Dependencies | Dependencias Operativas

- PostgreSQL in `5434`
- RabbitMQ in `5672`
- Stripe API or test credentials

Logstash is not a functional requirement for the payment API to respond.
