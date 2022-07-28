package com.eleks.academy.whoami.repository.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.state.impl.WaitingForPlayers;
import org.springframework.stereotype.Repository;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.repository.GameRepository;

@Repository
public class GameInMemoryRepository implements GameRepository {

	private final Map<String, SynchronousGame> games = new ConcurrentHashMap<>();

	@Override
	public Stream<SynchronousGame> findAllAvailable(String player) {
		Predicate<SynchronousGame> freeToJoin = game -> game.getState() instanceof WaitingForPlayers;
		return this.games
				.values()
				.stream()
				.filter(freeToJoin);

	}
	
	public Stream<SynchronousGame> findAllGames(String player) {
		return this.games
				.values()
				.stream();
	}

	@Override
	public Integer getAllPlayers() {
		return this.games
				.values()
				.stream()
				.map(game -> game.getPlayersList().size())
				.reduce(Integer::sum)
				.orElse(0);
	}

	@Override
	public void disbandGame(String id) {
		this.games.remove(id);
	}

	@Override
	public SynchronousGame save(SynchronousGame game) {
		this.games.put(game.getId(), game);
		return game;
	}

	@Override
	public Optional<SynchronousGame> findGameById(String id) {
		return Optional.ofNullable(this.games.get(id));
	}
	
	@Override
	public Optional<SynchronousPlayer> findPlayerById(String player) {
		return this.games
				.values()
				.stream()
				.map(game -> game.findPlayer(player))
				.findFirst()
				.orElse(Optional.empty());
	}

	@Override
	public Map<String, SynchronousGame> findAvailableQuickGames() {
		return filterByValue(games, game -> game.getState() instanceof WaitingForPlayers);
	}
	
	private <K, V> Map<K, V> filterByValue(Map<K, V> map, Predicate<V> predicate) {
	    return map.entrySet()
	            .stream()
	            .filter(entry -> predicate.test(entry.getValue()))
	            .collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue));
	}

}
