package com.example.Immobi.Dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: QuanNH
 * Response DTO for authentication operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    /**
     * JWT token for authentication
     */
    private String token;
    
    /**
     * Username of the authenticated user
     */
    private String username;
} 