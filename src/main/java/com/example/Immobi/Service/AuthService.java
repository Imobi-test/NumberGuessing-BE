package com.example.Immobi.Service;

import com.example.Immobi.Dto.auth.AuthResponse;
import com.example.Immobi.Dto.auth.LoginRequest;
import com.example.Immobi.Dto.auth.RegisterRequest;
import com.example.Immobi.Core.exception.BusinessException;
import com.example.Immobi.Core.exception.ErrorCode;
import com.example.Immobi.Core.security.JwtUtil;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Author: QuanNH
 * Service for handling authentication operations
 * Manages user registration and login processes
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Constructor with dependency injection
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user in the system
     * 
     * @param request Registration details
     * @return Authentication response with JWT token
     * @throws BusinessException if username already exists
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Create new user
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail()
        );

        // Save user to database
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(token, user.getUsername());
    }

    /**
     * Authenticates a user and creates a session
     * 
     * @param request Login credentials
     * @return Authentication response with JWT token
     * @throws BusinessException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user from authenticated object
            User user = (User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            return new AuthResponse(token, user.getUsername());
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password", e);
        }
    }
} 