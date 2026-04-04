package com.microcommerce.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration
 * Configuracion de MongoDB
 *
 * Enables MongoDB auditing for automatic population of @CreatedDate and @LastModifiedDate fields.
 * Habilita auditoria de MongoDB para la poblacion automatica de campos @CreatedDate y @LastModifiedDate.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
