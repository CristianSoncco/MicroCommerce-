package com.microcommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Order Service Application
 * Servicio de Gestión de Pedidos
 * 
 * Microservicio responsable de gestionar el ciclo de vida completo de los pedidos,
 * incluyendo creación, actualización, consulta y orquestación con otros servicios.
 * 
 * @author MicroCommerce Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
