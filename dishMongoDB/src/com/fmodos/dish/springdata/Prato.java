package com.fmodos.dish.springdata;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * @author fmodos
 *
 */
@Document
public class Prato {

	@Id
	private String id;

	private String nome;

	private int views = 0;

	private Set<String> ingredients = new HashSet<>();

	@DBRef
	private Chef chef;

	public Prato() {
	}

	public Prato(String nome) {
		this.nome = nome;
	}

	public Prato(String nome, Set<String> ingredients) {
		this.nome = nome;
		this.ingredients = ingredients;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Set<String> getIngredients() {
		return ingredients;
	}

	public void setIngredients(Set<String> ingredients) {
		this.ingredients = ingredients;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public Chef getChef() {
		return chef;
	}

	public void setChef(Chef chef) {
		this.chef = chef;
	}

}
