package com.eleks.academy.whoami.core.state;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.eleks.academy.whoami.core.SynchronousPlayer;
import com.eleks.academy.whoami.core.exception.GameException;
import com.eleks.academy.whoami.core.impl.GameCharacter;

public final class SuggestingCharacters extends AbstractGameState {

	private final Map<String, SynchronousPlayer> players;
	
	private final Map<String, List<GameCharacter>> suggestedCharacters;
	
	private final Map<String, String> playerCharacterMap;

	public SuggestingCharacters(Map<String, SynchronousPlayer> players) {
		super(players.size(), players.size());

		this.players = players;
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
				.filter(SuggestingCharacters::isReadyToNextState)
				.map(SuggestingCharacters::assign)
				.map(then -> new ProcessingQuestion(this.players))
				.orElseThrow(() -> new GameException("Cannot start game " + suggestedCharacters.size()));
	}
	
	@Override
	public GameState getCurrentState() {
		return this;
	}
	
	@Override
	public Stream<SynchronousPlayer> getPlayersList() {
		return this.players.values().stream();
	}
	
	@Override
	public boolean isReadyToNextState() {
		if (this.players
				.values()
				.stream()
				.filter(SynchronousPlayer::isSuggest)
				.count() == this.players.size()) {
			
			return true;
		} else 
			throw new GameException("Game not ready to start");
	}
	
	@Override
	public Optional<SynchronousPlayer> findPlayer(String player) {
		return Optional.ofNullable(this.players.get(player));
	}
	
	@Override
	public Optional<SynchronousPlayer> remove(String player) {
		// TODO Auto-generated method stub
		throw new ResponseStatusException(HttpStatus.FORBIDDEN);
	}

	private GameState assign() {
		
		final var authors = this.players.keySet().stream().toList();
		
		int i = 0;
		while (i < 3) {
			this.players.get(authors.get(i)).setGameCharacter(this.players.get(authors.get(i+1)).getCharacterSuggestion());
			i++;
		}
		
		this.players.get(authors.get(3)).setGameCharacter(this.players.get(authors.get(0)).getCharacterSuggestion());
		
		if (!isAllPlayersAssigned()) {
			throw new GameException("isAllPlayersAssigned = FALSE");
		}
		return this;
	}
	
	private boolean isAllPlayersAssigned() {
		return this.players
				.values()
				.stream()
				.filter(SynchronousPlayer::isCharacterAssigned)
				.count() == this.players.size();
	}
	
	private void suggestCharacter(SynchronousPlayer player) {
		List<GameCharacter> characters = this.suggestedCharacters.get(player.getUserName());

		if (Objects.isNull(characters)) {
			final var newCharacters = new ArrayList<GameCharacter>();

			this.suggestedCharacters.put(player.getUserName(), newCharacters);

			characters = newCharacters;
		}

		characters.add(GameCharacter.of(player.getCharacterSuggestion(), player.getUserName()));

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
					
					this.players.get(player).setGameCharacter(character.getCharacter());
					
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
