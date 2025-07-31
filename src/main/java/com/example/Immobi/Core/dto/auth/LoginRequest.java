package com.example.Immobi.Core.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author: QuanNH
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
} 