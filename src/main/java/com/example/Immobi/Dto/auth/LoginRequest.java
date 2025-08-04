package com.example.Immobi.Dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author: QuanNH
 * Request DTO for user login
 */
@Data
public class LoginRequest {
    
    /**
     * Username for login
     */
    @NotBlank(message = "Username is required")
    private String username;
    
    /**
     * Password for login
     */
    @NotBlank(message = "Password is required")
    private String password;
} 