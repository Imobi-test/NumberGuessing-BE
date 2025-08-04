package com.example.Immobi.Dto.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for leaderboard entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private Long userId;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Player's score
     */
    private int score;
    
    /**
     * Player's rank on leaderboard
     */
    private int rank;
} 