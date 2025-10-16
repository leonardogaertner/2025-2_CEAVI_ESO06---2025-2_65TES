package com.gerenciador.reservas.model;

/**
 * Representa uma sala de reunião com suas informações básicas.
 * Esta é uma classe de dados simples (POJO).
 */
public class Sala {

    private String id;
    private String nome;
    private int capacidade;

    // Construtor para facilitar a criação de objetos Sala
    public Sala(String id, String nome, int capacidade) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
    }

    // Getters e Setters para acessar e modificar os atributos privados
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

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }
}