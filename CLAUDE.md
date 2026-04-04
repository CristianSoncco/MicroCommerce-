# MicroCommerce - Contexto para Claude Code

## Descripcion
Arquitectura de microservicios e-commerce en Java 17 con Spring Boot. Proyecto portfolio de Cristian Soncco para Technologies Partner.

## Reglas de trabajo
- Logs y mensajes de error en español
- Documentacion bilingue (ES/EN)
- Sin emoticonos en documentacion
- Conventional Commits obligatorio
- Coverage minimo 80% en tests
- Cada commit debe compilar sin errores

## Stack
- Java 17, Spring Boot 3
- PostgreSQL (Product Service, User Service)
- MongoDB (Order Service)
- Redis (caching, Cache-Aside pattern)
- RabbitMQ (eventos)
- Eureka Server (discovery)
- Config Server (configuracion centralizada)
- API Gateway (punto de entrada unico)
- Docker Compose para desarrollo
- JUnit 5, Mockito, TestContainers para tests
- Swagger para documentacion API

## Estrategia Git
- Rama principal de desarrollo: develop
- Ramas de feature: feat/nombre-feature
- Squash merge a develop, luego a main
- Abrir PR a develop al terminar cada commit

## Checklist de commits

### Fase 1: Infraestructura [COMPLETADO]
- [x] COMMIT 1: Estructura base del proyecto
- [x] COMMIT 2: Eureka Server
- [x] COMMIT 3: Config Server
- [x] COMMIT 4: API Gateway

### Fase 2: Product Service [COMPLETADO]
- [x] COMMIT 5: Entidades y Repository
- [x] COMMIT 6: Service Layer con Redis
- [x] COMMIT 7: REST Controller
- [x] COMMIT 8: Tests (Unit + Integration)

### Fase 3: Order Service [EN PROGRESO]
- [x] COMMIT 9: Entidades y Repository (MongoDB)
- [x] COMMIT 10: Service Layer
- [x] COMMIT 11: REST Controller
- [ ] COMMIT 12: Tests

### Fase 4: User Service [PENDIENTE]
- [ ] COMMIT 13: Entidades y Repository
- [ ] COMMIT 14: Service Layer con JWT
- [ ] COMMIT 15: REST Controller
- [ ] COMMIT 16: Tests

### Fase 5: Payment Service [PENDIENTE]
- [ ] COMMIT 17: Integracion con API externa
- [ ] COMMIT 18: Service Layer
- [ ] COMMIT 19: REST Controller
- [ ] COMMIT 20: Tests

### Fase 6: Features Avanzadas [PENDIENTE]
- [ ] COMMIT 21: Event-Driven con RabbitMQ
- [ ] COMMIT 22: Logging centralizado (ELK)
- [ ] COMMIT 23: Monitoring (Prometheus + Grafana)
- [ ] COMMIT 24: API Versioning

### Fase 7: Kubernetes y DevOps [PENDIENTE]
- [ ] COMMIT 25: Kubernetes manifests
- [ ] COMMIT 26: Helm charts
- [ ] COMMIT 27: CI/CD Pipeline (GitHub Actions)

## Estado actual
- Ultimo commit completado: COMMIT 11 - REST Controller Order Service
- Rama activa: feat/order-rest-controller
- Proximo objetivo: COMMIT 12 - Tests Order Service

## Al terminar cada commit
1. Verificar que compila sin errores
2. Verificar que los tests pasan
3. Actualizar este CLAUDE.md marcando el commit como completado
4. Hacer commit con mensaje segun Conventional Commits
5. Push a la rama actual
6. Abrir PR a develop
