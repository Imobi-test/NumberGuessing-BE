package com.example.Immobi.Dto.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for player profile information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfileDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private Long id;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Player's score
     */
    private int score;
    
    /**
     * Remaining turns
     */
    private int remainingTurns;
    
    /**
     * Player's rank on leaderboard
     */
    private Integer rank;
} 