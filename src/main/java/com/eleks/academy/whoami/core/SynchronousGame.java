package com.eleks.academy.whoami.core;

import java.util.List;
import java.util.Optional;

import com.eleks.academy.whoami.model.request.GuessAnswer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.model.response.TurnDetails;

public interface SynchronousGame {

	String getId();

	GameState getState();

	List<PlayerWithState> getPlayersList();

	GameHistory getGameHistory();

	Optional<SynchronousPlayer> findPlayer(String player);

	TurnDetails findTurnInfo(String player);

	SynchronousPlayer enrollToGame(String player);

	Optional<SynchronousPlayer> leaveGame(String player);

	SynchronousGame start();

	void suggestCharacter(String player, CharacterSuggestion suggestion);

    void askQuestion(String player, String message);

	void answerQuestion(String player, QuestionAnswer answer);

    void submitGuess(String player, String guess);

    void answerGuess(String player, GuessAnswer answer);
}
