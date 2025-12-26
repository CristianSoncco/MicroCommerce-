# Eureka Server | Servidor Eureka

Service Discovery server for MicroCommerce microservices architecture.  
Servidor de descubrimiento de servicios para la arquitectura de microservicios de MicroCommerce.

---

## Description | Descripción

**EN:** Eureka Server provides service registry and discovery capabilities for all microservices in the platform. Services register themselves on startup and discover other services dynamically.

**ES:** Eureka Server proporciona capacidades de registro y descubrimiento de servicios para todos los microservicios de la plataforma. Los servicios se registran automáticamente al iniciar y descubren otros servicios dinámicamente.

---

## Technology Stack | Stack Tecnológico

- Spring Boot 3.2
- Spring Cloud Netflix Eureka Server
- Java 17

---

## Configuration | Configuración

- **Port | Puerto**: 8761
- **Dashboard URL**: http://localhost:8761
- **Actuator Health Check**: http://localhost:8761/actuator/health

---

## Running Locally | Ejecución Local

### Prerequisites | Prerequisitos
- Java 17+
- Maven 3.9+

### Run with Maven | Ejecutar con Maven
```bash
cd eureka-server
mvn spring-boot:run
```

### Run with Docker | Ejecutar con Docker
```bash
cd eureka-server
docker build -t eureka-server .
docker run -p 8761:8761 eureka-server
```

---

## Accessing the Dashboard | Acceder al Dashboard

**EN:** Once running, access the Eureka Dashboard at:  
**ES:** Una vez en ejecución, accede al Dashboard de Eureka en:

```
http://localhost:8761
```

**EN:** You will see:  
**ES:** Verás:

- Registered instances | Instancias registradas
- System status | Estado del sistema
- General info about Eureka Server | Información general del servidor Eureka

---

## Health Check | Verificación de Salud

**EN:** Check if the service is healthy:  
**ES:** Verifica si el servicio está saludable:

```bash
curl http://localhost:8761/actuator/health
```

**EN:** Expected response:  
**ES:** Respuesta esperada:

```json
{
  "status": "UP"
}
```

---

## Configuration Details | Detalles de Configuración

### Self-Preservation Mode | Modo de Auto-Preservación

**EN:** Disabled in development for faster eviction of failed instances. In production, this should be enabled to handle network partitions.

**ES:** Deshabilitado en desarrollo para una eliminación más rápida de instancias fallidas. En producción, esto debería estar habilitado para manejar particiones de red.

### Eviction Interval | Intervalo de Eliminación

**EN:** Set to 10 seconds for faster removal of dead instances in development. Default is 60 seconds.

**ES:** Configurado en 10 segundos para una eliminación más rápida de instancias muertas en desarrollo. El valor predeterminado es 60 segundos.

---

## Docker Build

**EN:** Multi-stage build optimizes image size:  
**ES:** La construcción multi-etapa optimiza el tamaño de la imagen:

- **Build stage | Etapa de construcción**: Uses JDK to compile | Usa JDK para compilar
- **Runtime stage | Etapa de ejecución**: Uses lightweight JRE (Alpine-based) | Usa JRE ligero (basado en Alpine)

---

## Integration with Other Services | Integración con Otros Servicios

**EN:** All microservices should include:  
**ES:** Todos los microservicios deben incluir:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**EN:** And enable discovery client:  
**ES:** Y habilitar el cliente de descubrimiento:

```java
@EnableDiscoveryClient
public class YourServiceApplication {
    // ...
}
```

---

## Monitoring | Monitoreo

**EN:** Available actuator endpoints:  
**ES:** Endpoints de actuator disponibles:

- `/actuator/health` - Health status | Estado de salud
- `/actuator/info` - Application info | Información de la aplicación
- `/actuator/metrics` - Metrics data | Datos de métricas

---

## Notes | Notas

**EN:**
- Eureka Server does NOT register itself with Eureka
- Self-preservation is disabled for development
- Services are evicted faster in development (10s vs 60s default)

**ES:**
- Eureka Server NO se registra a sí mismo en Eureka
- La auto-preservación está deshabilitada para desarrollo
- Los servicios se eliminan más rápido en desarrollo (10s vs 60s por defecto)
