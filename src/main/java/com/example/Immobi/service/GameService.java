package com.example.Immobi.service;

import com.example.Immobi.Core.dto.game.GuessRequest;
import com.example.Immobi.Core.dto.game.GuessResponse;
import com.example.Immobi.Core.exception.BusinessException;
import com.example.Immobi.Core.exception.ErrorCode;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Repository.GameStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

/**
 * Author: QuanNH
 * Service for number guessing game operations
 */
@Service
public class GameService {
    
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_THRESHOLD = 5; // 0-4 = win (5% of 0-99)

    private final GameStatsRepository gameStatsRepository;
    private final Random randomGenerator;

    public GameService(GameStatsRepository gameStatsRepository) {
        this.gameStatsRepository = gameStatsRepository;
        this.randomGenerator = new Random();
    }

    /**
     * Process a player's guess with fixed 5% win rate
     * Uses SERIALIZABLE isolation level and pessimistic locking to handle concurrent requests
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public GuessResponse processGuess(User player, GuessRequest guessRequest) {
        GameStats playerStats = getPlayerStatsWithLock(player);
        validatePlayerHasTurnsRemaining(playerStats);
        
        int guessedNumber = guessRequest.getNumber();
        int generatedNumber;
        boolean isCorrect;
        
        if (determineWinByProbability()) {
            generatedNumber = guessedNumber;
            isCorrect = true;
        } else {
            generatedNumber = generateDifferentNumber(guessedNumber);
            isCorrect = false;
        }
        
        playerStats.consumeTurn();
        if (isCorrect) {
            playerStats.awardPoint();
        }
        gameStatsRepository.save(playerStats);
        
        return createGuessResponse(
            isCorrect,
            generatedNumber, 
            guessedNumber, 
            playerStats.getRemainingTurns(), 
            playerStats.getScore()
        );
    }
    
    /**
     * Fixed 5% probability for player to win
     */
    private boolean determineWinByProbability() {
        int randomValue = randomGenerator.nextInt(100);
        return randomValue < WIN_THRESHOLD;
    }
    
    /**
     * Generate a number different from the player's guess
     */
    private int generateDifferentNumber(int guessedNumber) {
        int[] possibleNumbers = new int[MAX_NUMBER - MIN_NUMBER];
        int index = 0;
        
        for (int i = MIN_NUMBER; i <= MAX_NUMBER; i++) {
            if (i != guessedNumber) {
                possibleNumbers[index++] = i;
            }
        }
        
        return possibleNumbers[randomGenerator.nextInt(possibleNumbers.length)];
    }
    
    private GameStats getPlayerStatsWithLock(User player) {
        return gameStatsRepository.findByUserIdWithLock(player.getId())
                .orElseGet(() -> createNewPlayerStats(player));
    }
    
    private GameStats createNewPlayerStats(User player) {
        GameStats newStats = new GameStats(player);
        return gameStatsRepository.save(newStats);
    }
    
    private void validatePlayerHasTurnsRemaining(GameStats playerStats) {
        if (!playerStats.hasTurnsRemaining()) {
            throw new BusinessException(ErrorCode.NO_TURNS_LEFT, "No turns remaining");
        }
    }
    
    private GuessResponse createGuessResponse(boolean isCorrect, int generatedNumber, 
                                             int guessedNumber, int remainingTurns, int score) {
        String message = isCorrect 
                ? "Congratulations! You guessed correctly!" 
                : "Sorry, wrong guess. The number was " + generatedNumber;
        
        return GuessResponse.builder()
                .correct(isCorrect)
                .generatedNumber(generatedNumber)
                .guessedNumber(guessedNumber)
                .remainingTurns(remainingTurns)
                .score(score)
                .message(message)
                .build();
    }
    
}