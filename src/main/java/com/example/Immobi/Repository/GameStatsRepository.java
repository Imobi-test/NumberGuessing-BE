package com.example.Immobi.Repository;

import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Author: QuanNH
 * Repository for GameStats entity operations
 */
public interface GameStatsRepository extends JpaRepository<GameStats, Long> {
    
    /**
     * Find game stats by player
     * 
     * @param player The player
     * @return Optional GameStats for the player
     */
    Optional<GameStats> findByPlayer(User player);
    
    /**
     * Find player's game stats with pessimistic lock to prevent concurrent modifications
     * 
     * @param playerId Player's ID
     * @return Optional GameStats with lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM GameStats g WHERE g.player.id = :playerId")
    Optional<GameStats> findByUserIdWithLock(@Param("playerId") Long playerId);
    
    /**
     * Check if player has game stats
     * 
     * @param player The player
     * @return True if player has game stats
     */
    boolean existsByPlayer(User player);
} 