package com.example.Immobi.Core.exception;

/**
 * Author: QuanNH
 * Standardized error codes for the application
 */
public enum ErrorCode {
    // Authentication errors (1000-1099)
    AUTHENTICATION_FAILED(1000, "Authentication failed"),
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    USER_NOT_FOUND(1002, "User not found"),
    USERNAME_ALREADY_EXISTS(1003, "Username already exists"),
    TOKEN_EXPIRED(1004, "Token has expired"),
    INVALID_TOKEN(1005, "Invalid token"),
    
    // Validation errors (1100-1199)
    VALIDATION_ERROR(1100, "Validation error"),
    MISSING_REQUIRED_FIELD(1101, "Required field is missing"),
    INVALID_FIELD_FORMAT(1102, "Field format is invalid"),
    
    // Game errors (1200-1299)
    NO_TURNS_LEFT(1200, "No turns remaining"),
    INVALID_GUESS_NUMBER(1201, "Number must be between 1 and 5"),
    GAME_STATE_ERROR(1202, "Invalid game state"),
    CONCURRENT_REQUEST_ERROR(1203, "Too many concurrent requests"),
    PLAYER_STATS_NOT_FOUND(1204, "Player statistics not found"),
    
    // Server errors (2000-2099)
    INTERNAL_SERVER_ERROR(2000, "Internal server error"),
    SERVICE_UNAVAILABLE(2001, "Service temporarily unavailable"),
    DATABASE_ERROR(2002, "Database operation failed"),
    
    // Generic errors (9000-9099)
    UNKNOWN_ERROR(9000, "Unknown error occurred"),
    OPERATION_FAILED(9001, "Operation failed"),
    REQUEST_TIMEOUT(9002, "Request timed out");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}