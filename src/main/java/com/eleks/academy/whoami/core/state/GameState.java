package com.eleks.academy.whoami.core.state;

import java.util.Optional;
import java.util.stream.Stream;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.response.PlayerWithState;

public interface GameState {

	/**
	 * Used for presentation purposes only
	 *
	 * @return state of the current game
	 */
	GameState getCurrentState();
	
	/**
	 * Used for presentation purposes only
	 *
	 * @return {@code true} if meet all requirements to start a game
	 * or {@code false} if not
	 * 
	 */
	boolean isReadyToNextState();
	
	/*
	 * Switch to next game state if all tests passed 
	 *
	 * @return nextGameState
	 * @throw ResponseStatusException(400)
	 */
	GameState next();
	
	/*
	 * Return stream of players for better representation
	 * in response models 
	 *
	 * @return values of players map
	 */
	Stream<PlayerWithState> getPlayersList();

	/* @return player
	 * @throw ResponseStatusException(404) -> PlayerNotFoundException
	 */
	Optional<SynchronousPlayer> findPlayer(String player);

	/* @return player that was removed from game
	 * @throw ResponseStatusException(404) -> PlayerNotFoundException
	 */	
	Optional<SynchronousPlayer> leave(String player);
	
}
