package com.example.Immobi;

import com.example.Immobi.Service.LeaderboardService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

/**
 * Author: QuanNH
 * Main application class for the Immobi number guessing game.
 */
@SpringBootApplication
@EnableCaching
public class ImmobiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImmobiApplication.class, args);
	}

	/**
	 * Initialize application data at startup
	 */
	@Bean
	public CommandLineRunner initializeData(LeaderboardService leaderboardService) {
		return args -> {
			// Initialize leaderboard from database at startup
			leaderboardService.initializeLeaderboard();
		};
	}

}
