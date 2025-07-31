package com.example.Immobi.Service;

import com.example.Immobi.Core.dto.game.GuessRequest;
import com.example.Immobi.Dto.game.GuessResponse;
import com.example.Immobi.Core.exception.BusinessException;
import com.example.Immobi.Core.exception.ErrorCode;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Repository.GameStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_THRESHOLD = 5; // 0-4 = win (5% of 0-99)
    private static final int TURNS_TO_ADD = 5;

    private final GameStatsRepository gameStatsRepository;
    private final PlayerService playerService;
    private final LeaderboardService leaderboardService;
    private final Random randomGenerator;

    public GameService(GameStatsRepository gameStatsRepository, 
                      PlayerService playerService,
                      LeaderboardService leaderboardService) {
        this.gameStatsRepository = gameStatsRepository;
        this.playerService = playerService;
        this.leaderboardService = leaderboardService;
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
        boolean scoreChanged = false;
        
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
            scoreChanged = true;
        }
        
        // Save changes to database
        GameStats updatedStats = gameStatsRepository.save(playerStats);
        
        // If score changed, update Redis Sorted Set for real-time leaderboard
        if (scoreChanged) {
            leaderboardService.updatePlayerScore(player.getId(), player.getUsername(), updatedStats.getScore());
            log.debug("Updated score in leaderboard for user ID: {}", player.getId());
        } else {
            // Only profile needs refresh for turn count change
            playerService.refreshPlayerProfile(player.getId());
        }
        
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
        GameStats savedStats = gameStatsRepository.save(newStats);
        
        // Initialize player in leaderboard with score 0
        leaderboardService.updatePlayerScore(player.getId(), player.getUsername(), 0);
        
        return savedStats;
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
    
    /**
     * Buy additional turns for the player
     * @param player The player buying turns
     * @return Updated game stats with new turns count
     */
    @Transactional
    public GameStats buyAdditionalTurns(User player) {
        GameStats playerStats = getOrInitializePlayerStats(player);
        
        // TODO: Implement payment processing with VNPAY, PAYPAL, MOMO, etc.
        
        // Add turns after successful payment
        playerStats.addTurns(TURNS_TO_ADD);
        
        // Save changes to database
        GameStats updatedStats = gameStatsRepository.save(playerStats);
        
        // Update player profile in cache (turns count changed)
        playerService.refreshPlayerProfile(player.getId());
        
        return updatedStats;
    }
    
    @Transactional
    public GameStats resetPlayerTurns(User player) {
        GameStats playerStats = getOrInitializePlayerStats(player);
        playerStats.resetTurns();
        
        // Save changes to database
        GameStats updatedStats = gameStatsRepository.save(playerStats);
        
        // Update player profile in cache (turns count changed)
        playerService.refreshPlayerProfile(player.getId());
        
        return updatedStats;
    }

    @Transactional
    public GameStats getOrInitializePlayerStats(User player) {
        return gameStatsRepository.findByPlayer(player)
                .orElseGet(() -> createNewPlayerStats(player));
    }
}