package com.eleks.academy.whoami.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameNotFoundException;
import com.eleks.academy.whoami.core.exception.PlayerAlreadyInGameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.core.impl.PersistentGame;
import com.eleks.academy.whoami.core.state.SuggestingCharacters;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.NewGameRequest;
import com.eleks.academy.whoami.model.response.AllFields;
import com.eleks.academy.whoami.model.response.GameDetails;
import com.eleks.academy.whoami.model.response.GameLight;
import com.eleks.academy.whoami.model.response.LeaveModel;
import com.eleks.academy.whoami.model.response.PlayerSuggestion;
import com.eleks.academy.whoami.model.response.QuickGame;
import com.eleks.academy.whoami.model.response.StartGameModel;
import com.eleks.academy.whoami.model.response.TurnDetails;
import com.eleks.academy.whoami.repository.GameRepository;
import com.eleks.academy.whoami.service.GameService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

	private final GameRepository gameRepository;

	@Override
	public List<GameLight> findAvailableGames(String player) {
		return this.gameRepository.findAllAvailable(player).map(GameLight::of).toList();
	}

	@Override
	public GameDetails createGame(String player, NewGameRequest gameRequest) {
		final var game = this.gameRepository.save(new PersistentGame(player, gameRequest.getMaxPlayers()));

		return GameDetails.of(game);
	}

	//TODO: implement validations for create custom game
	@Override
	public SynchronousPlayer enrollToGame(String id, String player) {
		
		if (this.gameRepository.findPlayerByHeader(player).isPresent()) {
			throw new PlayerAlreadyInGameException("ENROLL-TO-GAME: [" + player + "] already in other game.");
		} else this.gameRepository.savePlayer(player);
		
		return this.gameRepository.findById(id).get().enrollToGame(player);
	}

	@Override
	public Optional<GameDetails> findByIdAndPlayer(String id, String player) {
		return this.gameRepository.findById(id).filter(game -> game.findPlayer(player).isPresent())
				.map(GameDetails::of);
	}

	/*
	 *TODO: check gameState 
	 */
	@Override
	public Optional<PlayerSuggestion> suggestCharacter(String id, String player, CharacterSuggestion suggestion) {

		this.gameRepository.findById(id)
				.filter(g -> !g.isAvailable() && g.getState() instanceof SuggestingCharacters)
				.map(game -> game.findPlayer(player))
				.ifPresentOrElse(p -> p.ifPresentOrElse(then -> then.suggest(suggestion), 
											() -> {
												throw new PlayerNotFoundException("SUGGESTING-CHARACTERS: [" + player + "] in game with id[" + id + "] not found.");
											}
										), 
						() -> {
							throw new GameNotFoundException("SUGGESTING-CHARACTERS: Game with id[" + id + "] not found.");
						}
				);
		
		SynchronousPlayer ingamePlayer = gameRepository.findById(id).flatMap(game -> game.findPlayer(player)).get();
		return Optional.of(PlayerSuggestion.of(ingamePlayer));
	}

	@Override
	public Optional<StartGameModel> startGame(String id, String player) {
		return this.gameRepository.findById(id)
							.filter(g -> g.getState().isReadyToNextState() && g.getState() instanceof SuggestingCharacters)
							.map(SynchronousGame::start)
							.map(StartGameModel::of);
	}

	@Override
	public void askQuestion(String gameId, String player, String message) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public Optional<TurnDetails> findTurnInfo(String id, String player) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public void submitGuess(String id, String player, String guess) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public void answerQuestion(String id, String player, String answer) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public Optional<QuickGame> findQuickGame(String player) {
		
		if (this.gameRepository.findPlayerByHeader(player).isEmpty()) {
			
			Map<String, SynchronousGame> games = gameRepository.findAvailableQuickGames();
			
			if (games.isEmpty()) {
				
				final SynchronousGame game = gameRepository.save(new PersistentGame(4));
				enrollToGame(game.getId(), player);
				
				return gameRepository.findById(game.getId()).map(QuickGame::of);
			}
			
			var FirstGame = games.keySet().stream().findFirst().get();
			enrollToGame(games.get(FirstGame).getId(), player);
			
			return gameRepository.findById(games.get(FirstGame).getId()).map(QuickGame::of);
		} else throw new PlayerAlreadyInGameException("QUICK-GAME: [" + player + "] already in other game.");
	}

	@Override
	public Optional<LeaveModel> leaveGame(String id, String player) {
		
		if (this.gameRepository.findPlayerByHeader(player).isPresent()) {

			var game = this.gameRepository.findById(id);
			
			if (game.isPresent()) {
				
				this.gameRepository.deletePlayerByHeader(player);
				return Optional.of(LeaveModel.of(game.get().deletePlayerFromGame(player).get(), id));
				
			} else throw new GameNotFoundException("Game with id[" + id + "] not found.");
			
		} else throw new PlayerNotFoundException("[" + player + "] in game with id[" + id + "] not found.");

	}

	@Override
	public List<AllFields> findAllGamesInfo(String player) {
		return this.gameRepository.findAllGames(player).map(AllFields::of).toList();
	}

	@Override
	public Integer getAllPlayersCount() {
		return Math.toIntExact(this.gameRepository.getAllPlayers().count());
	}

}
