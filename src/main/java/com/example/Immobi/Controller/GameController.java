package com.example.Immobi.Controller;

import com.example.Immobi.Core.dto.BaseResponse;
import com.example.Immobi.Dto.game.BuyTurnsResponse;
import com.example.Immobi.Core.dto.game.GuessRequest;
import com.example.Immobi.Dto.game.GuessResponse;
import com.example.Immobi.Entity.GameStats;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Author: QuanNH
 * REST controller for number guessing game operations
 */
@RestController
@RequestMapping("/api/game")
@Tag(name = "Number Guessing Game", description = "API endpoints for number guessing game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Process a player's guess
     */
    @PostMapping("/guess")
    @Operation(
        summary = "Make a guess", 
        description = "Guess a number between " + 
                      GuessRequest.MIN_GUESS_NUMBER + " and " + 
                      GuessRequest.MAX_GUESS_NUMBER
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guess processed successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or no turns left"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<BaseResponse<GuessResponse>> makeGuess(
            @AuthenticationPrincipal User player,
            @Valid @RequestBody GuessRequest guessRequest) {
        
        GuessResponse result = gameService.processGuess(player, guessRequest);
        
        String responseMessage = createResponseMessage(result.isCorrect());
        
        return ResponseEntity.ok(BaseResponse.success(result, responseMessage));
    }
    
    /**
     * Create appropriate response message based on guess result
     */
    private String createResponseMessage(boolean isCorrectGuess) {
        return isCorrectGuess 
                ? "Correct guess! You earned a point." 
                : "Wrong guess. Try again.";
    }
    
    /**
     * Buy additional turns for the player
     */
    @PostMapping("/buy-turns")
    @Operation(summary = "Buy additional turns", description = "Purchase 5 more turns")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Turns purchased successfully", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "402", description = "Payment required/failed")
    })
    public ResponseEntity<BaseResponse<BuyTurnsResponse>> buyTurns(@AuthenticationPrincipal User player) {
        GameStats updatedStats = gameService.buyAdditionalTurns(player);
        
        BuyTurnsResponse response = BuyTurnsResponse.builder()
                .successful(true)
                .newTurnCount(updatedStats.getRemainingTurns())
                .turnsAdded(5)
                .currentScore(updatedStats.getScore())
                .paymentMessage("Payment successful. 5 turns have been added to your account.")
                .build();
        
        return ResponseEntity.ok(BaseResponse.success(response, "Turns purchased successfully"));
    }
    
    @PostMapping("/reset")
    @Operation(summary = "Reset turns", description = "Reset player's turns to default value")
    public ResponseEntity<BaseResponse<Void>> resetTurns(@AuthenticationPrincipal User player) {
        gameService.resetPlayerTurns(player);
        return ResponseEntity.ok(BaseResponse.success(null, "Turns reset successfully"));
    }
} 