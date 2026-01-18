package com.microcommerce.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration
 * Configuración de OpenAPI/Swagger
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8081");
        localServer.setDescription("Servidor de desarrollo local | Local development server");

        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8080/api/products");
        gatewayServer.setDescription("A través del API Gateway | Through API Gateway");

        Contact contact = new Contact();
        contact.setName("MicroCommerce Team");
        contact.setEmail("contact@microcommerce.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Product Service API | API de Servicio de Productos")
                .version("1.0.0")
                .description("""
                    API REST para la gestión del catálogo de productos.
                    Proporciona operaciones CRUD, búsqueda, gestión de stock y caché con Redis.
                    
                    REST API for product catalog management.
                    Provides CRUD operations, search, stock management and Redis caching.
                    """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, gatewayServer));
    }
}
