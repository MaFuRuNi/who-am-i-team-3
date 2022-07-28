package com.eleks.academy.whoami.service.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.model.request.NewGameSize;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameShortInfo;
import com.eleks.academy.whoami.repository.impl.GameInMemoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

	private final GameInMemoryRepository mockGameRepository = mock(GameInMemoryRepository.class);	
	private final GameServiceImpl gameService = new GameServiceImpl(mockGameRepository);
	private final NewGameSize gameRequest = new NewGameSize();

	@BeforeEach
	void setup() {
		gameRequest.setMaxPlayers(2);
	}

	@Test
	void findAvailableGamesSuccessfulTest() {
		String player = "player";
		Stream<SynchronousGame> games = Stream.empty();

		when(mockGameRepository.findAllAvailable(player)).thenReturn(games);

		List<GameShortInfo> listOfGames = gameService.findAvailableGames(player);

		assertThat(listOfGames).isNotNull();
		assertThat(listOfGames).isEmpty();

		verify(mockGameRepository, times(1)).findAllAvailable(player);
	}

	@Test
	void createGameSuccessfulTest() {
		final String player = "player";
		final SynchronousGame game = new PersistentGame(gameRequest.getMaxPlayers());

		when(mockGameRepository.save(any(SynchronousGame.class))).thenReturn(game);

		Optional<GameDetails> createdGame = gameService.createGame(player, gameRequest);

		assertThat(createdGame).isNotNull();
		assertThat(createdGame.get().getId()).isEqualTo(game.getId());

		verify(mockGameRepository, times(1)).save(any(SynchronousGame.class));
	}
	
  @Test
	void leaveGameSuccessfulTest() {
		SynchronousGame game = new PersistentGame(4);
		Optional<SynchronousGame> gameById = Optional.of(game);

		when(mockGameRepository.findGameById(game.getId())).thenReturn(gameById);
		
		Assertions.assertDoesNotThrow(() -> gameService.leaveGame(game.getId(), "player"));
		
		verify(mockGameRepository, times(1)).findGameById(game.getId());
	}
	
  @Test
	void leaveGameFailedWithNotFoundTest() {
		final String id = "542332";
		Optional<SynchronousGame> gameById = Optional.empty();
		
		when(mockGameRepository.findGameById(eq(id))).thenReturn(gameById);
		
		Assertions.assertThrows(ResponseStatusException.class, () -> gameService.leaveGame(id, "player"));
		
		verify(mockGameRepository, times(1)).findGameById(eq(id));
	}
  
}
