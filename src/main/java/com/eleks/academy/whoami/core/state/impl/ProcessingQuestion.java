package com.eleks.academy.whoami.core.state.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.model.request.GuessAnswer;
import com.eleks.academy.whoami.model.response.GameHistory;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.request.QuestionAnswer;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;
import com.eleks.academy.whoami.model.response.TurnDetails;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public final class ProcessingQuestion implements GameState {

    private static final String QUESTION = "question";

    private static final String GUESS = "guess";

    private final GameHistory history = new GameHistory();

    private final Map<String, PlayerWithState> players;

    private final Queue<PlayerWithState> playerAskOrder;

    private String currentPlayer;

    public ProcessingQuestion(Map<String, PlayerWithState> players) {
        this.players = players;
        this.playerAskOrder = new LinkedBlockingQueue<>(this.players.values());
        startNewTurn();
    }

    @Override
    public GameState getCurrentState() {
        return this;
    }

    @Override
    public Stream<PlayerWithState> getPlayersList() {
        return this.players.values().stream();
    }

    public GameHistory getGameHistory() {
        return this.history;
    }

    public TurnDetails getTurnInfo() {
        return new TurnDetails(this.players.get(currentPlayer).getPlayer(),
                new ArrayList<>(this.players.values()));
    }

    public void askQuestion(String player, String message) {
        if (!this.players.get(player).getState().equals(PlayerState.ASKING)) {
            throw new PlayerNotFoundException("PROCESSING-QUESTION: [" + player + "] state [" +
                    this.players.get(player).getState().toString() + "] != Asking");
        }
        this.history.addQuestion(player, QUESTION, message);
        this.players.get(player).setState(PlayerState.ASKED);
    }

    public void answerQuestion(String player, QuestionAnswer answer) {
        if (this.history.getCurrentQuestion() == null) {
            throw new GameException("Player not provide a question yet.");
        }
        if (this.history.getCurrentQuestion().getType().equals(GUESS)) {
            throw new GameException("Current question type = " +
                    this.history.getCurrentQuestion().getType());
        }
        if (!this.players.get(player).getState().equals(PlayerState.ANSWERING)) {
            throw new PlayerNotFoundException("PROCESSING-QUESTION: [" + player + "] state [" +
                    this.players.get(player).getState().toString() + "] != ANSWERING.");
        }

        this.history.addAnswer(player, answer.toString());
        this.players.get(player).setAnswer(answer);
        this.players.get(player).setState(PlayerState.ANSWERED);

        if (isTimeToCalcAnswers()) {

            if (isTimeToChangeTurn()) {
                startNewTurn();
            } else reset();

        }

    }

    private boolean isTimeToCalcAnswers() {
        return this.playerAskOrder.size() ==
                this.playerAskOrder
                        .stream()
                        .filter(player -> player.getState().equals(PlayerState.ANSWERED))
                        .count() + 1;
    }

    private boolean isTimeToChangeTurn() {
        long yes = this.playerAskOrder
                .stream()
                .filter(player -> !player.getPlayer().getId().equals(this.currentPlayer))
                .filter(player -> player.getAnswer().equals(QuestionAnswer.YES) ||
                        player.getAnswer().equals(QuestionAnswer.NOT_SURE)
                )
                .count();

        long no = this.playerAskOrder
                .stream()
                .filter(player -> !player.getPlayer().getId().equals(this.currentPlayer))
                .filter(player -> player.getAnswer().equals(QuestionAnswer.NO))
                .count();

        System.out.println("[YES] = " + yes + " [NO] = " + no);
        return no > yes;
    }

    private void startNewTurn() {
        if (this.playerAskOrder.size() == 1) {
            var loser = this.playerAskOrder.peek().getPlayer().getId();
            this.players.get(loser).setState(PlayerState.LOST);
            this.players.get(loser).setAnswer(null);
            return;
        }
        this.currentPlayer = Objects.requireNonNull(this.playerAskOrder.poll()).getPlayer().getId();
        this.playerAskOrder.add(this.players.get(this.currentPlayer));
        this.history.setCurrentQuestion(null);
        reset();
    }

    private void reset() {
        this.history.setCurrentQuestion(null);
        this.players.values()
                .stream()
                .filter(player -> !player.getState().equals(PlayerState.FINISHED))
                .forEach(player -> {
                    if (player.getPlayer().getId().equals(currentPlayer)) {
                        player.setState(PlayerState.ASKING);
                        player.setAnswer(null);
                    } else {
                        player.setState(PlayerState.ANSWERING);
                        player.setAnswer(null);
                    }
                });
    }

    public void submitGuess(String player, String guess) {
        if (!this.players.get(player).getState().equals(PlayerState.ASKING)) {
            throw new PlayerNotFoundException("PROCESSING-QUESTION: [" + player + "] state [" +
                    this.players.get(player).getState().toString() + "] != Asking");
        }
        this.history.addQuestion(player, GUESS, guess);
        this.players.get(player).setState(PlayerState.ASKED);
    }

    public void answerGuess(String player, GuessAnswer answer) {
        if (this.history.getCurrentQuestion() == null) {
            throw new GameException("Player not provide a question yet.");
        }
        if (this.history.getCurrentQuestion().getType().equals(QUESTION)) {
            throw new GameException("Current question type = " +
                    this.history.getCurrentQuestion().getType());
        }
        if (!this.players.get(player).getState().equals(PlayerState.ANSWERING)) {
            throw new PlayerNotFoundException("PROCESSING-QUESTION: [" + player + "] state [" +
                    this.players.get(player).getState().toString() + "] != ANSWERING.");
        }

        this.history.addAnswer(player, answer.toString());
        this.players.get(player).setAnswer(QuestionAnswer.valueOf(answer.toString()));
        this.players.get(player).setState(PlayerState.ANSWERED);

        if (isTimeToCalcAnswers()) {

            if (isPlayerGuessed()) {
                this.players.get(this.currentPlayer).setState(PlayerState.FINISHED);
                this.playerAskOrder.remove(this.players.get(this.currentPlayer));
                startNewTurn();
            } else startNewTurn();

        }
    }

    private boolean isPlayerGuessed() {
        long yes = this.playerAskOrder
                .stream()
                .filter(player -> !player.getPlayer().getId().equals(this.currentPlayer))
                .filter(player -> player.getAnswer().equals(QuestionAnswer.YES))
                .count();

        long no = this.playerAskOrder
                .stream()
                .filter(player -> !player.getPlayer().getId().equals(this.currentPlayer))
                .filter(player -> player.getAnswer().equals(QuestionAnswer.NO))
                .count();

        return yes > no;
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
    public GameState next() {
        throw new GameException("Not implemented");
    }

    @Override
    public boolean isReadyToNextState() {
        return false;
    }

    @Override
    public Optional<SynchronousPlayer> leave(String player) {
        if (findPlayer(player).isPresent()) {
            var leavePlayer = this.players.remove(player);
            this.playerAskOrder.remove(leavePlayer);
            if (player.equals(this.currentPlayer)) {
                startNewTurn();
            } else reset();
            return Optional.of(leavePlayer.getPlayer());
        } else throw new PlayerNotFoundException("[" + player + "] not found.");
    }
}
