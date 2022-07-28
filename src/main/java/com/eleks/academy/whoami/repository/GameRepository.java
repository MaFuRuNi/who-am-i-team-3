package com.eleks.academy.whoami.repository;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface GameRepository {

	Stream<SynchronousGame> findAllAvailable(String player);

	SynchronousGame save(SynchronousGame game);

	Optional<SynchronousGame> findGameById(String id);

	Map<String, SynchronousGame> findAvailableQuickGames();

	Optional<SynchronousPlayer> findPlayerById(String player);

	Stream<SynchronousGame> findAllGames(String player);

    Integer getAllPlayers();

    void disbandGame(String id);

}
