package com.eleks.academy.whoami.core.impl;

import com.eleks.academy.whoami.core.SynchronousPlayer;

import java.util.Objects;

public class PersistentPlayer implements SynchronousPlayer {

	private String name;

	private String character;

	private final String id;

	public PersistentPlayer(String id, String name) {
		this.id = Objects.requireNonNull(id);
		this.name = Objects.requireNonNull(name);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setCharacter(String character) {
		this.character = character;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getCharacter() {
		return character;
	}

	@Override
	public String getId() {
		return id;
	}

}
