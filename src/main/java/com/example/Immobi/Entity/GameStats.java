package com.example.Immobi.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Author: QuanNH
 * Entity for tracking player's game statistics
 */
@Entity
@Table(name = "game_stats")
@NoArgsConstructor
@Getter
@Setter
public class GameStats {
    
    private static final int DEFAULT_INITIAL_TURNS = 5;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User player;
    
    @Column(nullable = false)
    private Integer score = 0;
    
    @Column(nullable = false)
    private Integer remainingTurns = DEFAULT_INITIAL_TURNS;
    
    @Version
    private Integer version;
    
    /**
     * Create game stats for a new player
     * 
     * @param player The player to create stats for
     */
    public GameStats(User player) {
        this.player = player;
        this.score = 0;
        this.remainingTurns = DEFAULT_INITIAL_TURNS;
    }
    
    /**
     * Check if player has turns remaining
     * 
     * @return True if player has at least one turn left
     */
    public boolean hasTurnsRemaining() {
        return remainingTurns > 0;
    }
    
    /**
     * Award a point to the player
     */
    public void awardPoint() {
        this.score++;
    }
    
    /**
     * Consume one turn
     * 
     * @return True if turn was successfully consumed
     */
    public boolean consumeTurn() {
        if (!hasTurnsRemaining()) {
            return false;
        }
        this.remainingTurns--;
        return true;
    }
    
    /**
     * Reset turns to default value
     */
    public void resetTurns() {
        this.remainingTurns = DEFAULT_INITIAL_TURNS;
    }
    
    /**
     * Add additional turns 
     * 
     * @param turnsToAdd Number of turns to add
     */
    public void addTurns(int turnsToAdd) {
        if (turnsToAdd > 0) {
            this.remainingTurns += turnsToAdd;
        }
    }
} 