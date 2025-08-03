package com.example.Immobi.Core.config.trigger;

import com.example.Immobi.Entity.User;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Repository.GameStatsRepository;
import com.example.Immobi.Service.LeaderboardService;
import jakarta.persistence.PostPersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Author: QuanNH
 * Entity listener for User entity to handle post-persist operations
 */
@Component
public class UserEntityListener {

    private GameStatsRepository gameStatsRepository;
    private LeaderboardService leaderboardService;

    @Autowired
    public void setGameStatsRepository(GameStatsRepository gameStatsRepository) {
        this.gameStatsRepository = gameStatsRepository;
    }

    @Autowired
    public void setLeaderboardService(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /**
     * Automatically create GameStats when a new User is persisted
     */
    @PostPersist
    public void createGameStats(User user) {
        try {
            if (gameStatsRepository != null) {
                // Create new GameStats for this user
                GameStats gameStats = new GameStats(user);
                gameStatsRepository.save(gameStats);
                
                // Also initialize in leaderboard
                if (leaderboardService != null) {
                    leaderboardService.updatePlayerScore(user.getId(), user.getUsername(), 0);
                }
            }
        } catch (Exception e) {
            // Log error but don't throw exception to avoid transaction rollback
            System.err.println("Error creating GameStats for user " + user.getUsername() + ": " + e.getMessage());
        }
    }
} 