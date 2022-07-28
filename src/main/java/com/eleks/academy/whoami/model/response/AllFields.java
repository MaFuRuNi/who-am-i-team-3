package com.eleks.academy.whoami.model.response;

import java.util.List;

import com.eleks.academy.whoami.core.SynchronousGame;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllFields {

private String id;
	
	private String status;
	
	private String playersInGame;
	
	private List<PlayerWithState> players;
	
	public static AllFields of(SynchronousGame game) {
		return AllFields.builder()
				.id(game.getId())
				.status(game.getState().getClass().getName())
				.playersInGame(String.valueOf(game.getPlayersList().size()))
				.players(game.getPlayersList())
				.build();
	}
}
