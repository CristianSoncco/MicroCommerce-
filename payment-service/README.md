# Payment Service | Servicio de Pagos

## Descripcion | Description

**ES:** Microservicio encargado de procesar pagos, consultar su estado y emitir eventos de pago para el resto de la plataforma.

**EN:** Microservice responsible for processing payments, querying their status, and publishing payment events to the rest of the platform.

---

## Estado Funcional

- Persistencia en PostgreSQL
- Integracion con Stripe mediante `WebClient`
- Publicacion de eventos `PAYMENT_COMPLETED`, `PAYMENT_FAILED` y `PAYMENT_REFUNDED` por RabbitMQ
- Health checks y metricas con Actuator

---

## Contrato Importante

### `orderId`

`payment-service` usa `orderId` como `String` para mantenerse alineado con `order-service`, que persiste pedidos en MongoDB con IDs tipo `String`.

### `paymentToken`

El endpoint de creacion de pago requiere `paymentToken`. Sin ese campo, la peticion es invalida.

---

## Endpoints Principales

| Metodo | Endpoint | Descripcion |
|---|---|---|
| `GET` | `/api/payments` | Listar todos los pagos |
| `POST` | `/api/payments` | Procesar un nuevo pago |
| `GET` | `/api/payments/{id}` | Obtener pago por ID interno |
| `GET` | `/api/payments/order/{orderId}` | Obtener pagos por pedido |
| `GET` | `/api/payments/user/{userId}` | Obtener pagos por usuario |
| `GET` | `/api/payments/status/{status}` | Obtener pagos por estado |
| `POST` | `/api/payments/{id}/refund` | Reembolsar pago |
| `POST` | `/api/payments/{id}/cancel` | Cancelar pago pendiente |

---

## Ejemplos

### Crear pago

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

### Listar pagos

```bash
curl http://localhost:8084/api/payments
```

### Ver pagos por pedido

```bash
curl http://localhost:8084/api/payments/order/6a0c56d54ff9423cea4576c9
```

---

## Dependencias Operativas

- PostgreSQL en `5434`
- RabbitMQ en `5672`
- Stripe API o credenciales de prueba

Logstash no es requisito funcional para responder a la API de pagos.
