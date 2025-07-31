package com.example.Immobi.Core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *Author: QuanNH
 * Standardized error codes for the application
 */
@Getter
public enum ErrorCode {
    // Authentication errors (1000-1099)
    AUTHENTICATION_FAILED(1000, "Authentication failed"),
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    USER_NOT_FOUND(1002, "User not found"),
    USERNAME_ALREADY_EXISTS(1003, "Username already exists"),
    TOKEN_EXPIRED(1004, "Token has expired"),
    INVALID_TOKEN(1005, "Invalid token");
    
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}