package com.example.Immobi.Controller;

import com.example.Immobi.Core.dto.BaseResponse;
import com.example.Immobi.Dto.player.LeaderboardEntryDto;
import com.example.Immobi.Dto.player.PlayerProfileDto;
import com.example.Immobi.Entity.User;
import com.example.Immobi.Service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for player-related endpoints
 */
@RestController
@RequestMapping("/api/players")
@Tag(name = "Player", description = "API endpoints for player information and leaderboard")
public class PlayerController {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);
    private static final int DEFAULT_LEADERBOARD_SIZE = 10;
    private static final String CACHE_REFRESH_SUCCESS = "Cache refreshed successfully";

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Get player leaderboard
     */
    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard", description = "Get top players sorted by score")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully")
    })
    public ResponseEntity<BaseResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        log.info("Leaderboard request received for top {} players", limit);
        
        // Enforce reasonable limits
        
        List<LeaderboardEntryDto> leaderboard = playerService.getLeaderboard(DEFAULT_LEADERBOARD_SIZE);
        return ResponseEntity.ok(BaseResponse.success(
                leaderboard,
                String.format("Top %d players retrieved successfully", leaderboard.size())));
    }

    /**
     * Get current player profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get profile", description = "Get current player profile information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<BaseResponse<PlayerProfileDto>> getPlayerProfile(
            @AuthenticationPrincipal User user) {
        
        log.info("Profile request received for user: {}", user.getUsername());
        
        PlayerProfileDto profile = playerService.getPlayerProfile(user);
        return ResponseEntity.ok(BaseResponse.success(
                profile,
                "Profile retrieved successfully"));
    }
    
    /**
     * Force reset and rebuild leaderboard from database
     * Admin only endpoint for maintenance purposes
     */
    @PostMapping("/admin/reset-leaderboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset leaderboard", description = "Force rebuild leaderboard from database (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leaderboard reset successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<BaseResponse<Void>> resetLeaderboard() {
        log.info("Manual leaderboard reset requested");
        playerService.resetLeaderboard();
        return ResponseEntity.ok(BaseResponse.success(null, "Leaderboard reset successfully"));
    }
    
    /**
     * Force refresh player profile cache
     * Can be used by admins or by players for their own profile
     */
    @PostMapping("/refresh-profile")
    @Operation(summary = "Refresh profile cache", description = "Force refresh current user's profile cache")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<BaseResponse<Void>> refreshProfileCache(
            @AuthenticationPrincipal User user) {
        log.info("Manual profile cache refresh requested for user ID: {}", user.getId());
        playerService.refreshPlayerProfile(user.getId());
        return ResponseEntity.ok(BaseResponse.success(null, CACHE_REFRESH_SUCCESS));
    }
} 