# Centralized Logging | Logging Centralizado (ELK Stack)

## Descripcion | Description

**ES:** Este directorio contiene la configuracion del stack ELK (Elasticsearch, Logstash, Kibana) usado por MicroCommerce para centralizar los logs de todos los microservicios.

**EN:** This directory holds the ELK stack (Elasticsearch, Logstash, Kibana) configuration used by MicroCommerce to centralize logs from every microservice.

---

## Arquitectura | Architecture

```
Spring Boot Service (logstash-logback-encoder)
        |
        |  JSON over TCP :5000
        v
     Logstash (pipeline.conf)
        |
        |  HTTP :9200
        v
    Elasticsearch
        ^
        |  HTTP :9200
        |
      Kibana :5601 (UI)
```

---

## Componentes | Components

| Servicio | Puerto | Descripcion |
|----------|--------|-------------|
| Elasticsearch | 9200, 9300 | Almacen e indexador de logs |
| Logstash | 5000 (TCP), 9600 | Pipeline de ingesta |
| Kibana | 5601 | UI de visualizacion y busqueda |

---

## Como activar el envio de logs | How to ship logs

1. Arranca el stack de desarrollo:

   ```
   docker-compose -f docker-compose-dev.yml up -d elasticsearch logstash kibana
   ```

2. Exporta las variables de entorno antes de arrancar cada microservicio:

   ```
   export LOGSTASH_ENABLED=true
   export LOGSTASH_HOST=localhost
   export LOGSTASH_PORT=5000
   ```

3. Arranca el microservicio (Maven o JAR). Los logs se enviaran en formato JSON estructurado al indice `microcommerce-<service_name>-YYYY.MM.DD`.

---

## Estructura de indices | Index layout

Los logs se almacenan en indices diarios por servicio:

- `microcommerce-product-service-YYYY.MM.DD`
- `microcommerce-order-service-YYYY.MM.DD`
- `microcommerce-user-service-YYYY.MM.DD`
- `microcommerce-payment-service-YYYY.MM.DD`
- `microcommerce-api-gateway-YYYY.MM.DD`

Cada documento incluye los campos estandar de `LogstashEncoder` (`@timestamp`, `level`, `logger_name`, `thread_name`, `message`, `stack_trace`) mas los campos extra `service_name` y `environment`.

---

## Configuracion en Kibana | Configure Kibana

1. Abre `http://localhost:5601`.
2. Ve a `Stack Management > Data Views > Create data view`.
3. Usa el patron `microcommerce-*` y selecciona `@timestamp` como campo temporal.
4. Explora los logs en `Discover`.

---

## Archivos | Files

- `logstash/config/logstash.yml` - configuracion del proceso de Logstash.
- `logstash/pipeline/logstash.conf` - pipeline input/filter/output.
