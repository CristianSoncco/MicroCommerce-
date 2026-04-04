package com.microcommerce.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User creation and update
 * Objeto de Transferencia de Datos para creacion y actualizacion de User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email es invalido")
    @Size(max = 100, message = "El email no debe exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contrasena es requerida")
    @Size(min = 8, max = 100, message = "La contrasena debe tener entre 8 y 100 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String lastName;

    @Size(max = 20, message = "El telefono no debe exceder 20 caracteres")
    private String phone;

    @Size(max = 500, message = "La direccion no debe exceder 500 caracteres")
    private String address;
}
