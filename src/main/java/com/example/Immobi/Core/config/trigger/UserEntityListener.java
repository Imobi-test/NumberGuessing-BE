package com.example.Immobi.Core.config.trigger;

import com.example.Immobi.Dto.auth.CreatedEnventDto;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Repository.GameStatsRepository;
import com.example.Immobi.Service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Author: QuanNH
 * Entity listener for User entity to handle post-commit operations
 */
@Component
public class UserEntityListener {

    private final GameStatsRepository gameStatsRepository;
    private final LeaderboardService leaderboardService;

    private final Integer DEFAULT_SCORES = 0;

    @Autowired
    public UserEntityListener(@Lazy GameStatsRepository gameStatsRepository,
                            @Lazy LeaderboardService leaderboardService) {
        this.gameStatsRepository = gameStatsRepository;
        this.leaderboardService = leaderboardService;
    }

    /**
     * Automatically create GameStats when a new User is committed to database
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createGameStats(CreatedEnventDto event) {
        var user = event.getUser();
        try {
            if (gameStatsRepository != null) {
                // Create new GameStats for this user
                GameStats gameStats = new GameStats(user);
                gameStatsRepository.save(gameStats);
                
                // Also initialize in leaderboard
                if (leaderboardService != null) {
                    leaderboardService.updatePlayerScore(user.getId(), user.getUsername(), DEFAULT_SCORES);
                }
            }
        } catch (Exception e) {
            // Log error but don't throw exception to avoid transaction rollback
            System.err.println("Error creating GameStats for user " + user.getUsername() + ": " + e.getMessage());
        }
    }
} 