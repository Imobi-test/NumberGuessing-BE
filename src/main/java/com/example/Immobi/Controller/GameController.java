package com.example.Immobi.Controller;

import com.example.Immobi.Core.dto.BaseResponse;
import com.example.Immobi.Core.dto.game.GuessRequest;
import com.example.Immobi.Core.dto.game.GuessResponse;
import com.example.Immobi.Entity.User;
import com.example.Immobi.service.GameService;
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
     * 
     * @param player The authenticated player
     * @param guessRequest The guess request
     * @return Response with guess result
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
     * 
     * @param isCorrectGuess Whether the guess was correct
     * @return Response message
     */
    private String createResponseMessage(boolean isCorrectGuess) {
        return isCorrectGuess 
                ? "Correct guess! You earned a point." 
                : "Wrong guess. Try again.";
    }
    
    /**
     * Reset a player's turns to default value
     * 
     * @param player The authenticated player
     * @return Response indicating success
     */
    @PostMapping("/reset")
    @Operation(summary = "Reset turns", description = "Reset player's turns to default value")
    public ResponseEntity<BaseResponse<Void>> resetTurns(@AuthenticationPrincipal User player) {
        gameService.resetPlayerTurns(player);
        return ResponseEntity.ok(BaseResponse.success(null, "Turns reset successfully"));
    }
} 