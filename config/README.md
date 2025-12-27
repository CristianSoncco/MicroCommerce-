# Configuration Files | Archivos de Configuración

This directory contains centralized configuration files for all microservices.  
Este directorio contiene archivos de configuración centralizados para todos los microservicios.

---

## Structure | Estructura

```
config/
├── application.yml          # Shared configuration | Configuración compartida
├── product-service.yml      # Product Service config
├── order-service.yml        # Order Service config
├── user-service.yml         # User Service config
└── api-gateway.yml          # API Gateway config
```

---

## How It Works | Cómo Funciona

**EN:** Config Server reads these files and serves them to microservices on startup. Each service requests its configuration from Config Server using its `spring.application.name`.

**ES:** Config Server lee estos archivos y los sirve a los microservicios al iniciar. Cada servicio solicita su configuración de Config Server usando su `spring.application.name`.

---

## Configuration Priority | Prioridad de Configuración

**EN:** Configuration is loaded in this order (later overrides earlier):

**ES:** La configuración se carga en este orden (el posterior sobrescribe al anterior):

1. `application.yml` - Shared by all services | Compartida por todos los servicios
2. `{service-name}.yml` - Service-specific | Específica del servicio
3. Environment variables | Variables de entorno
4. Command line arguments | Argumentos de línea de comandos

---

## Adding New Service Configuration | Agregar Configuración de Nuevo Servicio

**EN:** Create a new file named `{service-name}.yml` where `{service-name}` matches the `spring.application.name` of your service.

**ES:** Crea un archivo nuevo llamado `{service-name}.yml` donde `{service-name}` coincida con el `spring.application.name` de tu servicio.

**Example | Ejemplo:**

```yaml
# config/payment-service.yml
server:
  port: 8084

spring:
  application:
    name: payment-service
```

---

## Environment-Specific Configuration | Configuración por Ambiente

**EN:** You can create environment-specific files:

**ES:** Puedes crear archivos específicos por ambiente:

```
config/
├── product-service.yml           # Default | Por defecto
├── product-service-dev.yml       # Development | Desarrollo
├── product-service-prod.yml      # Production | Producción
└── product-service-staging.yml   # Staging
```

**EN:** Activate with profile:

**ES:** Activar con perfil:

```bash
-Dspring.profiles.active=prod
```

---

## Security Notes | Notas de Seguridad

**EN:**
- Never commit sensitive passwords or keys
- Use environment variables for secrets
- Example: `${DATABASE_PASSWORD:default-value}`

**ES:**
- Nunca commitees contraseñas o claves sensibles
- Usa variables de entorno para secretos
- Ejemplo: `${DATABASE_PASSWORD:valor-por-defecto}`

---

## Testing Configuration | Probar Configuración

**EN:** Once Config Server is running, you can test configurations:

**ES:** Una vez que Config Server esté corriendo, puedes probar las configuraciones:

```bash
# Get product-service configuration
curl http://localhost:8888/product-service/default

# Get order-service configuration
curl http://localhost:8888/order-service/default

# Get shared configuration
curl http://localhost:8888/application/default
```

---

## References | Referencias

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Externalized Configuration Pattern](https://microservices.io/patterns/externalized-configuration.html)

