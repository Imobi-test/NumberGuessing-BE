package com.example.Immobi.Dto.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Author: QuanNH
 * Request DTO for number guessing game
 */
@Data
public class GuessRequest {
    
    /**
     * The minimum allowed number for guessing
     */
    public static final int MIN_GUESS_NUMBER = 1;
    
    /**
     * The maximum allowed number for guessing
     */
    public static final int MAX_GUESS_NUMBER = 5;
    
    @NotNull(message = "Guessed number is required")
    @Min(value = MIN_GUESS_NUMBER, message = "Number must be at least " + MIN_GUESS_NUMBER)
    @Max(value = MAX_GUESS_NUMBER, message = "Number must be at most " + MAX_GUESS_NUMBER)
    private Integer number;
} 