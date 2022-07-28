package com.eleks.academy.whoami.service;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.request.GuessAnswer;
import com.eleks.academy.whoami.model.request.NewGameSize;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.*;

import java.util.List;
import java.util.Optional;

public interface GameService {

	Integer getAllPlayersCount();

	List<GameShortInfo> findAvailableGames(String player);

	Optional<GameDetails> findByIdAndPlayer(String id, String player);

	List<AllFields> findAllGamesInfo(String player);

	Optional<TurnDetails> findTurnInfo(String id, String player);

	Optional<GameHistory> findGameHistory(String id);

	Optional<GameDetails> createGame(String player, NewGameSize gameRequest);

	SynchronousPlayer enrollToGame(String id, String player);

	Optional<LeaveDetails> leaveGame(String id, String player);

	void suggestCharacter(String id, String player, CharacterSuggestion suggestion);

	Optional<GameDetails> startGame(String id, String player);

	void askQuestion(String gameId, String player, String message);

	void answerQuestion(String id, String player, QuestionAnswer answer);

	void submitGuess(String id, String player, String guess);

    void answerGuess(String id, String player, GuessAnswer answer);
}
