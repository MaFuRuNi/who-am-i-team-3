package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.core.SynchronousGame;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface GameRepository {

	Stream<SynchronousGame> findAllAvailable(String player);

	SynchronousGame save(SynchronousGame game);

	Optional<SynchronousGame> findById(String id);

	Map<String, SynchronousGame> findAvailableQuickGames();

	Optional<String> findPlayerByHeader(String player);

	void savePlayer(String player);

	void deletePlayerByHeader(String player);

	Stream<SynchronousGame> findAllGames(String player);

    Stream<String> getAllPlayers();
}
