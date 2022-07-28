package com.eleks.academy.whoami.core.state.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class WaitingForPlayers implements GameState {

	private final int maxPlayers;
	private final Map<String, PlayerWithState> players;

	public WaitingForPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
		this.players = new HashMap<>(maxPlayers);
	}

	@Override
	public GameState next() {
		return Optional.of(this)
				.filter(WaitingForPlayers::isReadyToNextState)
				.map(then -> new SuggestingCharacters(this.players))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
	}

	@Override
	public Stream<PlayerWithState> getPlayersList() {
		return this.players.values().stream();
	}
	
	@Override
	public GameState getCurrentState() {
		return this;
	}

	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		var result = Optional.ofNullable(this.players.get(player));
		if (result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(result.get().getPlayer());
	}

	@Override
	public Optional<SynchronousPlayer> leave(String player) {
		if (findPlayer(player).isPresent()) {
			return Optional.of(this.players.remove(player).getPlayer());
		} else throw new PlayerNotFoundException("[" + player + "] not found.");
	}
	
	public SynchronousPlayer add(SynchronousPlayer player) {
		this.players.put(player.getId(),
				new PlayerWithState(player, null, PlayerState.NOT_READY));
		return player;
	}

	@Override
	public boolean isReadyToNextState() {
		return players.size() == maxPlayers;
	}

}
