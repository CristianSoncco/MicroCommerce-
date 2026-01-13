# Config Server | Servidor de Configuración

Centralized configuration server for MicroCommerce microservices architecture.  
Servidor de configuración centralizada para la arquitectura de microservicios de MicroCommerce.

---

## Description | Descripción

**EN:** Config Server provides centralized external configuration for all microservices. It reads configuration files from a Git repository and serves them via HTTP endpoints. Services fetch their configuration on startup, enabling configuration changes without recompilation.

**ES:** Config Server proporciona configuración externa centralizada para todos los microservicios. Lee archivos de configuración desde un repositorio Git y los sirve vía endpoints HTTP. Los servicios obtienen su configuración al iniciar, permitiendo cambios de configuración sin recompilar.

---

## Technology Stack | Stack Tecnológico

- Spring Boot 3.2
- Spring Cloud Config Server
- Spring Cloud Netflix Eureka Client
- Java 17

---

## Configuration | Configuración

- **Port | Puerto**: 8888
- **Git Backend**: Local file system | Sistema de archivos local
- **Config Location | Ubicación de Configs**: `../config/`
- **Eureka Registration | Registro en Eureka**: Yes | Sí
- **Actuator Health Check**: http://localhost:8888/actuator/health

---

## How It Works | Cómo Funciona

```
┌─────────────────┐
│ Product Service │
└────────┬────────┘
         │ 1. Request config
         │ GET /product-service/default
         ▼
┌──────────────────┐
│  Config Server   │ 2. Read from Git
│   (Port 8888)    │    file://../config/
└────────┬─────────┘
         │ 3. Return configuration
         ▼
┌─────────────────┐
│ Product Service │ 4. Start with config
└─────────────────┘
```

---

## Running Locally | Ejecución Local

### Prerequisites | Prerequisitos
- Java 17+
- Maven 3.9+
- Eureka Server running | Eureka Server corriendo

### Run with Maven | Ejecutar con Maven

```bash
cd config-server
mvn spring-boot:run
```

### Run with Docker | Ejecutar con Docker

```bash
cd config-server
docker build -t config-server .
docker run -p 8888:8888 config-server
```

---

## Testing Configuration Serving | Probar Servicio de Configuración

**EN:** Once Config Server is running, test if it serves configurations correctly:

**ES:** Una vez que Config Server esté corriendo, prueba si sirve las configuraciones correctamente:

### Get Product Service Configuration | Obtener Configuración del Product Service

```bash
curl http://localhost:8888/product-service/default
```

**EN:** Expected response: Configuration from `config/product-service.yml`

**ES:** Respuesta esperada: Configuración de `config/product-service.yml`

### Get Shared Configuration | Obtener Configuración Compartida

```bash
curl http://localhost:8888/application/default
```

**EN:** Expected response: Shared configuration from `config/application.yml`

**ES:** Respuesta esperada: Configuración compartida de `config/application.yml`

### Get All Available Configurations | Ver Todas las Configuraciones Disponibles

```bash
# Order Service
curl http://localhost:8888/order-service/default

# User Service
curl http://localhost:8888/user-service/default

# API Gateway
curl http://localhost:8888/api-gateway/default
```

---

## Configuration Files Location | Ubicación de Archivos de Configuración

**EN:** Configuration files are stored in:

**ES:** Los archivos de configuración están almacenados en:

```
MicroCommerce/
├── config/                      ← Configuration files | Archivos de configuración
│   ├── application.yml
│   ├── product-service.yml
│   ├── order-service.yml
│   ├── user-service.yml
│   └── api-gateway.yml
└── config-server/               ← Config Server application | Aplicación Config Server
```

---

## Health Check | Verificación de Salud

```bash
curl http://localhost:8888/actuator/health
```

**EN:** Expected response:

**ES:** Respuesta esperada:

```json
{
  "status": "UP"
}
```

---

## Environment Variables | Variables de Entorno

**EN:** You can override the Git branch using:

**ES:** Puedes sobrescribir la rama Git usando:

```bash
export GIT_BRANCH=main
mvn spring-boot:run
```

---

## Integration with Services | Integración con Servicios

**EN:** To connect a service to Config Server, add to its `application.yml`:

**ES:** Para conectar un servicio a Config Server, agrega a su `application.yml`:

```yaml
spring:
  application:
    name: product-service  # Must match config file name | Debe coincidir con nombre del archivo de config
  config:
    import: "configserver:http://localhost:8888"
```

**EN:** Or using bootstrap properties (older approach):

**ES:** O usando bootstrap properties (enfoque antiguo):

```yaml
# bootstrap.yml
spring:
  application:
    name: product-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
```

---

## Configuration Priority | Prioridad de Configuración

**EN:** Configuration is applied in this order (later overrides earlier):

**ES:** La configuración se aplica en este orden (el posterior sobrescribe al anterior):

1. `config/application.yml` - Shared configuration | Configuración compartida
2. `config/{service-name}.yml` - Service-specific | Específica del servicio
3. Environment variables in service | Variables de entorno en el servicio
4. Command-line arguments | Argumentos de línea de comandos

---

## Refresh Configuration Without Restart | Refrescar Configuración Sin Reiniciar

**EN:** Services can refresh configuration at runtime using Spring Cloud Bus or manually:

**ES:** Los servicios pueden refrescar configuración en tiempo de ejecución usando Spring Cloud Bus o manualmente:

```bash
# Manual refresh (requires @RefreshScope on beans)
curl -X POST http://localhost:8081/actuator/refresh
```

---

## Security Considerations | Consideraciones de Seguridad

**EN:**
- Never commit sensitive data (passwords, API keys) to Git
- Use environment variables: `${DATABASE_PASSWORD:default}`
- Enable encryption for sensitive properties in production
- Secure Config Server endpoints with Spring Security

**ES:**
- Nunca commitees datos sensibles (contraseñas, API keys) a Git
- Usa variables de entorno: `${DATABASE_PASSWORD:default}`
- Habilita encriptación para propiedades sensibles en producción
- Asegura endpoints de Config Server con Spring Security

---

## Monitoring | Monitoreo

**EN:** Available actuator endpoints:

**ES:** Endpoints de actuator disponibles:

- `/actuator/health` - Health status | Estado de salud
- `/actuator/info` - Application info | Información de la aplicación
- `/actuator/metrics` - Metrics data | Datos de métricas

---

## Troubleshooting | Resolución de Problemas

### Service Can't Connect | Servicio No Puede Conectar

**EN:** Ensure:
1. Config Server is running on port 8888
2. Service configuration points to correct Config Server URL
3. Config file name matches service's `spring.application.name`

**ES:** Asegúrate:
1. Config Server está corriendo en puerto 8888
2. Configuración del servicio apunta a URL correcta de Config Server
3. Nombre del archivo de config coincide con `spring.application.name` del servicio

### Configuration Not Loading | Configuración No Carga

**EN:** Check:
1. Config file exists in `config/` directory
2. YAML syntax is correct
3. Config Server logs for errors

**ES:** Verifica:
1. Archivo de config existe en directorio `config/`
2. Sintaxis YAML es correcta
3. Logs de Config Server por errores

---

## Docker Build

**EN:** Multi-stage build optimizes image size:

**ES:** La construcción multi-etapa optimiza el tamaño de la imagen:

- **Build stage | Etapa de construcción**: Uses JDK to compile | Usa JDK para compilar
- **Runtime stage | Etapa de ejecución**: Uses lightweight JRE (Alpine-based) | Usa JRE ligero (basado en Alpine)

---

## Notes | Notas

**EN:**
- Config Server registers itself with Eureka for service discovery
- Uses local Git repository (file system) for configuration storage
- Supports multiple profiles (dev, staging, prod)
- Configuration changes require service refresh or restart

**ES:**
- Config Server se registra en Eureka para descubrimiento de servicios
- Usa repositorio Git local (sistema de archivos) para almacenar configuración
- Soporta múltiples perfiles (dev, staging, prod)
- Cambios de configuración requieren refresh o reinicio del servicio

---

## References | Referencias

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Externalized Configuration Pattern](https://microservices.io/patterns/externalized-configuration.html)

