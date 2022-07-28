package com.eleks.academy.whoami.core.state.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.Optional;
import java.util.stream.Stream;

public final class GameFinished implements GameState {

	private final int playersInGame;

	private final int maxPlayers;

	public GameFinished(int playersInGame, int maxPlayers) {
		this.playersInGame = playersInGame;
		this.maxPlayers = maxPlayers;
	}

	@Override
	public GameState next() {
		return null;
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.empty();
	}

	@Override
	public GameState getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadyToNextState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<SynchronousPlayer> leave(String player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<PlayerWithState> getPlayersList() {
		// TODO Auto-generated method stub
		return null;
	}

}
