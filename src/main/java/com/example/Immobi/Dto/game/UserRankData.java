package com.example.Immobi.Dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: QuanNH
 * DTO class for user rank data used in leaderboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRankData {

    /**
     * User ID
     */
    private Long userId;
    
    /**
     * The username
     */
    private String username;
} 