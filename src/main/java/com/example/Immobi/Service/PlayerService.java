package com.example.Immobi.Service;

import com.example.Immobi.Core.config.RedisConfig;
import com.example.Immobi.Dto.player.LeaderboardEntryDto;
import com.example.Immobi.Dto.player.PlayerProfileDto;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Repository.GameStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for player-related operations
 */
@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private static final int DEFAULT_LEADERBOARD_SIZE = 10;
    
    private final GameStatsRepository gameStatsRepository;
    private final LeaderboardService leaderboardService;

    public PlayerService(GameStatsRepository gameStatsRepository, LeaderboardService leaderboardService) {
        this.gameStatsRepository = gameStatsRepository;
        this.leaderboardService = leaderboardService;
    }

    /**
     * Get top players for leaderboard
     * Uses Redis Sorted Set for efficient real-time ranking
     * 
     * @param limit Number of players to return (default 10)
     * @return List of leaderboard entries
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(int limit) {
        int leaderboardSize = limit > 0 ? limit : DEFAULT_LEADERBOARD_SIZE;
        log.info("Fetching top {} players for leaderboard", leaderboardSize);
        
        return leaderboardService.getTopPlayers(leaderboardSize);
    }
    
    /**
     * Get player profile with stats
     * Profile info still uses Redis cache for efficiency
     * 
     * @param user The user
     * @return Player profile DTO
     */
    @Cacheable(value = RedisConfig.CACHE_PLAYER_PROFILE, key = "#user.id")
    @Transactional(readOnly = true)
    public PlayerProfileDto getPlayerProfile(User user) {
        log.info("Fetching profile for user ID: {}", user.getId());
        
        // Get player stats
        Optional<GameStats> statsOptional = gameStatsRepository.findByPlayerIdWithPlayer(user.getId());
        
        // Get player rank from Redis leaderboard (much faster than database query)
        Integer rank = leaderboardService.getPlayerRank(user.getId());
        
        // Create profile DTO
        return statsOptional.map(stats -> PlayerProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .score(stats.getScore())
                .remainingTurns(stats.getRemainingTurns())
                .rank(rank)
                .build()
        ).orElseGet(() -> PlayerProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .score(0)
                .remainingTurns(0)
                .rank(null)
                .build()
        );
    }
    
    /**
     * Clear player profile cache when their data changes
     * 
     * @param userId ID of the user whose cache should be cleared
     */
    @CacheEvict(value = RedisConfig.CACHE_PLAYER_PROFILE, key = "#userId")
    public void refreshPlayerProfile(Long userId) {
        log.info("Player profile cache cleared for user ID: {}", userId);
    }
    
    /**
     * Reset the leaderboard data in Redis
     * This forces a refresh from the database
     */
    public void resetLeaderboard() {
        leaderboardService.resetLeaderboard();
    }
} 