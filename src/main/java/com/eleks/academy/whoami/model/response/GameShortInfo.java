package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.SynchronousGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameShortInfo {

	private String id;

	private String status;
	
	private String playersInGame;

	public static GameShortInfo of(SynchronousGame game) {
		return GameShortInfo.builder()
				.id(game.getId())
				.status(game.getState().getClass().getName())
				.playersInGame(String.valueOf(game.getPlayersList().size()))
				.build();
	}

}
