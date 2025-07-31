package com.example.Immobi.Dto.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: QuanNH
 * Response DTO when buying additional turns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyTurnsResponse {
    /**
     * Whether the purchase was successful
     */
    private boolean successful;
    
    /**
     * New turn count after purchase
     */
    private int newTurnCount;
    
    /**
     * Number of turns added in this purchase
     */
    private int turnsAdded;
    
    /**
     * Current score of the player
     */
    private int currentScore;
    
    /**
     * Payment status message
     */
    private String paymentMessage;
} 