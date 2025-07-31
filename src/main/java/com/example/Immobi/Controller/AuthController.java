package com.example.Immobi.Controller;

import com.example.Immobi.Core.dto.BaseResponse;
import com.example.Immobi.Dto.auth.AuthResponse;
import com.example.Immobi.Dto.auth.LoginRequest;
import com.example.Immobi.Dto.auth.RegisterRequest;
import com.example.Immobi.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Author: QuanNH
 * Controller for handling authentication requests
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor with dependency injection
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "User Register", description = "Creates a new user account and returns authentication token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
    })
    public ResponseEntity<BaseResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return new ResponseEntity<>(
            BaseResponse.success(authResponse, "User registered successfully"), 
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Validates credentials and returns authentication token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success(authResponse, "Login successful"));
    }
} 