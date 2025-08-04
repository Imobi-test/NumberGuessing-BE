package com.example.Immobi.Service;

import com.example.Immobi.Dto.player.LeaderboardEntryDto;
import com.example.Immobi.Dto.game.UserRankData;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Repository.GameStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing leaderboard using Redis Sorted Sets
 */
@Service
public class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);
    private static final String LEADERBOARD_KEY = "game:leaderboard";
    private static final int DEFAULT_CACHE_DAYS = 30;
    private static final String VALUE_DELIMITER = ":";

    private final RedisTemplate<String, Object> redisTemplate;
    private final GameStatsRepository gameStatsRepository;

    public LeaderboardService(RedisTemplate<String, Object> redisTemplate, 
                             GameStatsRepository gameStatsRepository) {
        this.redisTemplate = redisTemplate;
        this.gameStatsRepository = gameStatsRepository;
    }

    /**
     * Initialize leaderboard data from database if not exists in Redis
     */
    @Transactional(readOnly = true)
    public void initializeLeaderboard() {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(LEADERBOARD_KEY))) {
            log.info("Initializing leaderboard in Redis from database");
            
            List<GameStats> allStats = gameStatsRepository.findAll();
            
            if (!allStats.isEmpty()) {
                for (GameStats stats : allStats) {
                    User player = stats.getPlayer();
                    updatePlayerScore(player.getId(), player.getUsername(), stats.getScore());
                }
                
                redisTemplate.expire(LEADERBOARD_KEY, DEFAULT_CACHE_DAYS, TimeUnit.DAYS);
                
                log.info("Initialized leaderboard with {} players", allStats.size());
            }
        }
    }

    /**
     * Update a player's score in the leaderboard
     * Value format: userId:username
     */
    public void updatePlayerScore(Long userId, String username, int score) {
        try {
            String memberValue = encodeValue(userId, username);
            redisTemplate.opsForZSet().add(LEADERBOARD_KEY, memberValue, (double)score);
            log.debug("Updated score for user ID {} to {} in leaderboard", userId, score);
        } catch (Exception e) {
            log.error("Error updating leaderboard for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Get a player's rank in the leaderboard
     * @return 1-based rank or null if not found
     */
    public Integer getPlayerRank(Long userId) {
        try {
            Set<Object> allValues = redisTemplate.opsForZSet().reverseRange(LEADERBOARD_KEY, 0, -1);
            if (allValues == null || allValues.isEmpty()) {
                return null;
            }
            
            String userIdStr = String.valueOf(userId);
            int rank = 0;
            Double lastScore = null;
            int duplicateRankCounter = 0;
            
            for (Object value : allValues) {
                String memberValue = value.toString();
                String[] parts = memberValue.split(VALUE_DELIMITER);
                
                if (parts.length >= 1) {
                    String currentUserId = parts[0];
                    
                    if (userIdStr.equals(currentUserId)) {
                        return rank + 1; // 1-based rank
                    }
                    
                    Double currentScore = redisTemplate.opsForZSet().score(LEADERBOARD_KEY, memberValue);
                    if (Objects.equals(currentScore, lastScore)) {
                        duplicateRankCounter++;
                    } else {
                        rank += duplicateRankCounter + 1;
                        duplicateRankCounter = 0;
                        lastScore = currentScore;
                    }
                }
            }
            
            return null; // User not found
        } catch (Exception e) {
            log.error("Error getting rank for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get top players from the leaderboard (Redis only, no DB queries)
     */
    public List<LeaderboardEntryDto> getTopPlayers(int leaderboardSize) {
        List<LeaderboardEntryDto> leaderboard = new ArrayList<>();
        
        try {
            initializeLeaderboard();
            
            Set<ZSetOperations.TypedTuple<Object>> rangeWithScores = 
                redisTemplate.opsForZSet().reverseRangeWithScores(LEADERBOARD_KEY, 0, leaderboardSize - 1);
            
            if (!rangeWithScores.isEmpty()) {
                int rank = 1;
                Double previousScore = null;
                int sameScoreRank = 1;
                
                for (ZSetOperations.TypedTuple<Object> tuple : rangeWithScores) {
                    Object value = tuple.getValue();
                    Double score = tuple.getScore();
                    
                    if (value != null && score != null) {
                        String memberValue = value.toString();
                        UserRankData userRankData = decodeValue(memberValue);
                        
                        // Update rank only when score changes (dense ranking)
                        sameScoreRank = Objects.equals(previousScore, score) ? sameScoreRank : rank;
                        
                        leaderboard.add(LeaderboardEntryDto.builder()
                                .userId(userRankData.getUserId())
                                .username(userRankData.getUsername())
                                .score(score.intValue())
                                .rank(sameScoreRank)
                                .build());
                        
                        previousScore = score;
                        rank++;
                    }
                }
            }
            
            log.debug("Retrieved {} entries from leaderboard", leaderboard.size());
            return leaderboard;
            
        } catch (Exception e) {
            log.error("Error retrieving leaderboard: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Remove a player from the leaderboard
     */
    public void removePlayer(Long userId) {
        try {
            Set<Object> allValues = redisTemplate.opsForZSet().range(LEADERBOARD_KEY, 0, -1);
            if (allValues == null || allValues.isEmpty()) {
                return;
            }
            
            String userIdStr = String.valueOf(userId);
            for (Object value : allValues) {
                String memberValue = value.toString();
                String[] parts = memberValue.split(VALUE_DELIMITER);
                
                if (parts.length >= 1 && userIdStr.equals(parts[0])) {
                    redisTemplate.opsForZSet().remove(LEADERBOARD_KEY, memberValue);
                    log.debug("Removed user ID {} from leaderboard", userId);
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error removing user {} from leaderboard: {}", userId, e.getMessage(), e);
        }
    }

    public long getTotalPlayers() {
        try {
            Long size = redisTemplate.opsForZSet().size(LEADERBOARD_KEY);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Error getting leaderboard size: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    public void resetLeaderboard() {
        try {
            redisTemplate.delete(LEADERBOARD_KEY);
            log.info("Leaderboard has been reset");
            initializeLeaderboard();
        } catch (Exception e) {
            log.error("Error resetting leaderboard: {}", e.getMessage(), e);
        }
    }
    
    private String encodeValue(Long userId, String username) {
        return String.valueOf(userId) + VALUE_DELIMITER + username;
    }
    
    private UserRankData decodeValue(String value) {
        String[] parts = value.split(VALUE_DELIMITER, 2);
        Long userId = null;
        String username = "Unknown";
        
        if (parts.length >= 1) {
            try {
                userId = Long.valueOf(parts[0]);
            } catch (NumberFormatException e) {
                log.error("Error parsing user ID: {}", e.getMessage());
            }
        }
        
        if (parts.length >= 2) {
            username = parts[1];
        }
        
        return new UserRankData(userId, username);
    }

} 