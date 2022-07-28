package com.eleks.academy.whoami.core.state.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.exception.PlayerNotFoundException;
import com.eleks.academy.whoami.core.impl.GameCharacter;
import com.eleks.academy.whoami.core.state.GameState;
import com.eleks.academy.whoami.model.request.CharacterSuggestion;
import com.eleks.academy.whoami.model.response.PlayerState;
import com.eleks.academy.whoami.model.response.PlayerWithState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class SuggestingCharacters implements GameState {

    private final Map<String, PlayerWithState> players;

    private final Map<String, List<GameCharacter>> suggestedCharacters;

    private final Map<String, GameCharacter> suggestions;

    private final Map<String, String> playerCharacterMap;

    public SuggestingCharacters(Map<String, PlayerWithState> players) {
        this.players = new ConcurrentHashMap<>(players);
        this.suggestions = new HashMap<>(this.players.size());
        this.suggestedCharacters = new HashMap<>(this.players.size());
        this.playerCharacterMap = new ConcurrentHashMap<>(this.players.size());
    }

    /**
     * Randomly assigns characters to players and returns a next stage
     * or throws {@link GameException} in case returns {@code false}
     *
     * @return next {@link ProcessingQuestion} stage
     */
    @Override
    public GameState next() {
        return Optional.of(this)
                .map(SuggestingCharacters::assign)
                .map(then -> new ProcessingQuestion(this.players))
                .orElseThrow(() -> new GameException("Failed in next()-> PQ."));
    }

    @Override
    public GameState getCurrentState() {
        return this;
    }

    @Override
    public Stream<PlayerWithState> getPlayersList() {
        return this.players.values().stream();
    }

    @Override
    public boolean isReadyToNextState() {
        return this.players
                .values()
                .stream()
                .map(PlayerWithState::getState)
                .filter(state -> state.equals(PlayerState.READY))
                .count() == this.players.size();
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

    public void suggestCharacter(String player, CharacterSuggestion suggestion) {
        if (this.players.get(player).getState().equals(PlayerState.NOT_READY)) {
            var currentPlayer = this.players.get(player);

            this.suggestions.put(player, GameCharacter.of(suggestion.getCharacter(), player));

            currentPlayer.getPlayer().setName(suggestion.getName());
            currentPlayer.setState(PlayerState.READY);
        } else {
            throw new GameException("[" + player + "] already submit his suggestion.");
        }

    }

    private GameState assign() {

        for (PlayerWithState player : this.players.values()) {
            var character = findNonTakenCharacter(player.getPlayer());
            player.getPlayer().setCharacter(character.getCharacter());
        }

        if (!isAllPlayersAssigned()) {
            throw new GameException("isAllPlayersAssigned = FALSE");
        }
        return this;
    }

    private GameCharacter findNonTakenCharacter(SynchronousPlayer player) {
        var character = this.suggestions.values()
                .stream()
                .filter(c -> !c.isTaken() && !c.getAuthor().equals(player.getId()))
                .findAny();
        character.ifPresent(gameCharacter -> this.suggestions.get(gameCharacter.getAuthor()).markTaken());
        return character.orElse(null);
    }

    private boolean isAllPlayersAssigned() {
        return this.players
                .values()
                .stream()
                .map(PlayerWithState::getPlayer)
                .map(player -> player.getCharacter() != null)
                .count() == this.players.size();
    }

    private void suggestCharacter(SynchronousPlayer player) {
        List<GameCharacter> characters = this.suggestedCharacters.get(player.getId());

        if (Objects.isNull(characters)) {
            final var newCharacters = new ArrayList<GameCharacter>();

            this.suggestedCharacters.put(player.getId(), newCharacters);

            characters = newCharacters;
        }

        characters.add(GameCharacter.of(player.getCharacter(), player.getId()));

    }

    /**
     * The term author is referred to a player who suggested at least one character
     * <p>
     * Basic algorithm description:
     * 1) Collect all randomly-ordered authors into a form of cyclic oriented graph.
     * 2) Assign to each author a random character suggested by the next author (next graph node)
     * 3) Randomly assign all the suggested characters that are left disregarding the author to
     * all the non-author players
     */
    private GameState assignCharacters() {
        Function<String, Integer> randomAuthorOrderComparator = value ->
                Double.valueOf(Math.random() * 1000).intValue();

        final var authors =
                this.suggestedCharacters.keySet()
                        .stream()
                        .sorted(Comparator.comparing(randomAuthorOrderComparator))
                        .collect(Collectors.toList());

        authors.forEach(author -> {
            final var character = this.getRandomCharacter()
                    .apply(this.suggestedCharacters.get(this.<String>cyclicNext().apply(authors, author)));

            character.markTaken();

            this.playerCharacterMap.put(author, character.getCharacter());
        });

        final var authorsSet = new HashSet<>(authors);

        final var nonTakenCharacters = this.suggestedCharacters.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(character -> !character.isTaken())
                .collect(toList());

        this.players.keySet()
                .stream()
                .filter(player -> !authorsSet.contains(player))
                .forEach(player -> {
                    final var character = this.getRandomCharacter().apply(nonTakenCharacters);

                    character.markTaken();

                    this.players.get(player).getPlayer().setCharacter(character.getCharacter());

                    this.playerCharacterMap.put(player, character.getCharacter());

                    nonTakenCharacters.remove(character);
                });


        return this;
    }

    private Function<List<GameCharacter>, GameCharacter> getRandomCharacter() {
        return gameCharacters -> {
            int randomPos = (int) (Math.random() * gameCharacters.size());

            return gameCharacters.get(randomPos);
        };
    }

    private <T> BiFunction<List<T>, T, T> cyclicNext() {
        return (list, item) -> {
            final var index = list.indexOf(item);

            return Optional.of(index)
                    .filter(i -> i + 1 < list.size())
                    .map(i -> list.get(i + 1))
                    .orElseGet(() -> list.get(0));
        };
    }

}
