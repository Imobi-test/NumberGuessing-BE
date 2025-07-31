package com.example.Immobi.Repository;

import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameStatsRepository extends JpaRepository<GameStats, Long> {
    
    /**
     * Find stats by player with pessimistic write lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT gs FROM GameStats gs WHERE gs.player.id = :playerId")
    Optional<GameStats> findByUserIdWithLock(@Param("playerId") Long playerId);
    
    /**
     * Find stats by player
     */
    Optional<GameStats> findByPlayer(User player);
    
    /**
     * Find top players ordered by score
     * 
     * @param limit Number of players to return
     * @return List of GameStats for top players
     */
    @Query("SELECT gs FROM GameStats gs JOIN FETCH gs.player ORDER BY gs.score DESC")   
    List<GameStats> findTopPlayersByScore(int limit);
    
    /**
     * Find player stats by user id with eager loading of player data
     */
    @Query("SELECT gs FROM GameStats gs JOIN FETCH gs.player WHERE gs.player.id = :playerId")
    Optional<GameStats> findByPlayerIdWithPlayer(@Param("playerId") Long playerId);
    
    /**
     * Get player's rank based on score
     */
    @Query(value = "SELECT (COUNT(*) + 1) FROM game_stats WHERE score > (SELECT score FROM game_stats WHERE player_id = :playerId)", 
           nativeQuery = true)
    int getPlayerRank(@Param("playerId") Long playerId);
} 