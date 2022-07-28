package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousGame;
import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.model.request.GuessAnswer;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.core.exception.GameNotFoundException;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.core.state.impl.ProcessingQuestion;
import com.eleks.academy.whoami.core.state.impl.SuggestingCharacters;
import com.eleks.academy.whoami.core.state.impl.WaitingForPlayers;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.model.response.TurnDetails;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentGame implements SynchronousGame {

    private final String id;

    private final Queue<GameState> gameState = new LinkedBlockingQueue<>();

    private final List<String> listOfDefaultNames = List.of("Player 1", "Player 2", "Player 3", "Player 4");

    private final Queue<String> queue = new LinkedBlockingQueue<>(listOfDefaultNames);

    /*
     * Creates a new game (game room)
     *
     * @param maxPlayers to initiate a new game
     */
    public PersistentGame(Integer maxPlayers) {
        this.id = String.format("%d-%d", Instant.now().toEpochMilli(),
                Double.valueOf(Math.random() * 999).intValue());
        this.gameState.add(new WaitingForPlayers(maxPlayers));
    }

    /*
     * @return {@code String} game unique identifier
     */
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public GameState getState() {
        return this.applyIfPresent(this.gameState.peek(), GameState::getCurrentState);
    }

    @Override
    public List<PlayerWithState> getPlayersList() {
        return this.applyIfPresent(this.gameState.peek(), GameState::getPlayersList)
                .collect(Collectors.toList());
    }

    @Override
    public GameHistory getGameHistory() {
        return this.gameState.peek() instanceof ProcessingQuestion ?
                ((ProcessingQuestion) this.gameState.peek()).getGameHistory() : null;
    }

    @Override
    public SynchronousPlayer enrollToGame(String player) {
        if (this.getState() instanceof WaitingForPlayers) {

            var newPlayer = new PersistentPlayer(player, getDefaultName());

            assert gameState.peek() != null;
            ((WaitingForPlayers) gameState.peek()).add(newPlayer);

            assert gameState.peek() != null;
            if (String.valueOf(this.getPlayersList().size()).equals("4")) {
                this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
            }
            return newPlayer;
        } else
            throw new GameNotFoundException("Game [" + this.getId() + "] already at "
                    + this.getState().getClass().getSimpleName() + " state.");
    }

    private String getDefaultName() {
        String defaultName = this.queue.poll();
        this.queue.add(defaultName);
        return defaultName;
    }

    @Override
    public Optional<SynchronousPlayer> leaveGame(String player) {
        return this.applyIfPresent(this.gameState.peek(), gameState -> gameState.leave(player));
    }

    @Override
    public void suggestCharacter(String player, CharacterSuggestion suggestion) {
        if (findPlayer(player).isPresent()) {

            assert gameState.peek() != null;
            ((SuggestingCharacters) gameState.peek()).suggestCharacter(player, suggestion);

            assert gameState.peek() != null;
            if (gameState.peek().isReadyToNextState()) {
                this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
            }
        }
    }

    @Override
    public void askQuestion(String player, String message) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).askQuestion(player, message);
        } else throw new PlayerNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
    }

    @Override
    public void answerQuestion(String player, QuestionAnswer answer) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).answerQuestion(player, answer);
        } else throw new PlayerNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
    }

    @Override
    public void submitGuess(String player, String guess) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).submitGuess(player, guess);
        } else throw new PlayerNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
    }

    @Override
    public void answerGuess(String player, GuessAnswer answer) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            ((ProcessingQuestion) this.gameState.peek()).answerGuess(player, answer);
        } else throw new PlayerNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
    }

    @Override
    public Optional<SynchronousPlayer> findPlayer(String player) {
        return this.applyIfPresent(this.gameState.peek(), gameState -> gameState.findPlayer(player));
    }

    @Override
    public TurnDetails findTurnInfo(String player) {
        if (findPlayer(player).isPresent()) {
            assert this.gameState.peek() != null;
            return ((ProcessingQuestion) this.gameState.peek()).getTurnInfo();
        } else throw new PlayerNotFoundException("Game [" + this.getId() + "] not found [" + player + "].");
    }

    @Override
    public SynchronousGame start() {
        this.gameState.add(Objects.requireNonNull(this.gameState.poll()).next());
        return this;
    }

    private <T, R> R applyIfPresent(T source, Function<T, R> mapper) {
        return this.applyIfPresent(source, mapper, null);
    }

    private <T, R> R applyIfPresent(T source, Function<T, R> mapper, R fallback) {
        return Optional.ofNullable(source).map(mapper).orElse(fallback);
    }

}
