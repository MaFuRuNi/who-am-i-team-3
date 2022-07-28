package com.eleks.academy.whoami.controller;

import com.eleks.academy.whoami.configuration.GameControllerAdvice;
import com.eleks.academy.whoami.model.request.NewGameSize;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.LeaveDetails;
import com.eleks.academy.whoami.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

	private final GameServiceImpl gameService = mock(GameServiceImpl.class);
	private final GameController gameController = new GameController(gameService);
	private final NewGameSize gameRequest = new NewGameSize();
	private MockMvc mockMvc;

	@BeforeEach
	public void setMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(gameController)
				.setControllerAdvice(new GameControllerAdvice()).build();
		gameRequest.setMaxPlayers(3);
	}

	@Test
	void findAvailableGames() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders.get("/games")
								.header("X-Player", "player"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0]").doesNotHaveJsonPath());
	}

	@Test
	void createGame() throws Exception {
		GameDetails gameDetails = new GameDetails();
		gameDetails.setId("12613126");
		gameDetails.setStatus("WaitingForPlayers");
		when(gameService.createGame(eq("player"), any(NewGameSize.class))).thenReturn(Optional.of(gameDetails));
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "maxPlayers": 2
										}"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("id").value("12613126"))
				.andExpect(jsonPath("status").value("WaitingForPlayers"));
	}

	@Test
	void createGameFailedWithException() throws Exception {
		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games")
								.header("X-Player", "player")
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "maxPlayers": null
										}"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("{\"message\":\"Validation failed!\"," +
						"\"details\":[\"maxPlayers must not be null\"]}"));
	}
	
	@Test
	void suggestCharacter() throws Exception {
		
		final String header = "Test-Player";

		this.mockMvc.perform(
						MockMvcRequestBuilders.post("/games/1234/characters")
								.header("X-Player", header)
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{
										    "nickname": " Usop",
										    "character": " char"
										}"""))
				.andExpect(status().isOk());
	}
	
	@Test
	void leaveGameTest() throws Exception {
		final String id = "686863";
		
		Optional<LeaveDetails> response = Optional.of(new LeaveDetails("Test-Player", id));
		
		when(gameService.leaveGame(id, "Test-Player")).thenReturn(response);
		
		this.mockMvc.perform(MockMvcRequestBuilders.delete("/games/{id}/leave", id)
					.header("X-Player", "Test-Player")
					.content("""
							{
							    "username": " Test-Player",
							    "gameId": " {id}"
							}"""))
					.andExpect(status().isOk())
					.andExpect(jsonPath("username").value("Test-Player"))
					.andExpect(jsonPath("gameId").value(id));
		
		verify(gameService, times(1)).leaveGame(eq(id), eq("Test-Player"));
	}
  
}
