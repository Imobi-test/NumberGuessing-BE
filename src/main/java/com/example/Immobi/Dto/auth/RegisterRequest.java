package com.example.Immobi.Dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Author: QuanNH
 * Request DTO for user registration
 */
@Data
public class RegisterRequest {
    
    /**
     * Username for the new account
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;
    
    /**
     * Password for the new account
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
    
    /**
     * Email address for the new account
     */
    @Email(message = "Invalid email address")
    private String email;
    
} 