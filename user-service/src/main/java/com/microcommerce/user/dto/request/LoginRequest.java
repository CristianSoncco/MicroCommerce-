package com.microcommerce.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login request
 * DTO para solicitud de inicio de sesion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email es invalido")
    private String email;

    @NotBlank(message = "La contrasena es requerida")
    private String password;
}
