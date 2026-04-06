package com.microcommerce.payment.config;

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
 * Configuracion de OpenAPI/Swagger
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8084");
        localServer.setDescription("Servidor de desarrollo local | Local development server");

        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8080/api/payments");
        gatewayServer.setDescription("A traves del API Gateway | Through API Gateway");

        Contact contact = new Contact();
        contact.setName("MicroCommerce Team");
        contact.setEmail("contact@microcommerce.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Payment Service API | API de Servicio de Pagos")
                .version("1.0.0")
                .description("""
                    API REST para el procesamiento de pagos con integracion a Stripe.
                    Proporciona operaciones de cobro, reembolso y consulta de pagos.

                    REST API for payment processing with Stripe integration.
                    Provides charge, refund and payment query operations.
                    """)
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, gatewayServer));
    }
}
