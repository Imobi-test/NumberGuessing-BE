package com.example.Immobi;

import com.example.Immobi.Service.LeaderboardService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Author: QuanNH
 * Main application class for the Immobi number guessing game.
 */
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
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
			leaderboardService.initializeLeaderboard();
		};
	}

}
