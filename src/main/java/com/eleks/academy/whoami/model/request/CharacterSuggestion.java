package com.eleks.academy.whoami.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacterSuggestion {

	@NotBlank(message = "Name may not be blank")
	@Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters long")
	@Pattern(message = "Name contains special symbols.", regexp = "^\\w+\\s{0,5}\\w*$")
	private String name;

	@NotBlank(message = "Character may not be blank")
	@Size(min = 2, max = 50, message = "Character must be between 2 and 50 characters long")
	@Pattern(message = "Character contains special symbols.", regexp = "^\\w+\\s{0,5}\\w*$")
	private String character;
	
}
