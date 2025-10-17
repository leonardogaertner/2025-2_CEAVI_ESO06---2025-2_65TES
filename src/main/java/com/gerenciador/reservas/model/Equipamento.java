package com.gerenciador.reservas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // Define que esta classe é uma tabela no banco de dados
public class Equipamento {

    @Id // Marca o campo 'id' como a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.AUTO) // Define que o ID será gerado automaticamente
    private Long id; // É uma boa prática usar Long para IDs gerados automaticamente

    private String nome;
    private String descricao;

    // Construtor padrão exigido pelo JPA
    public Equipamento() {
    }

    public Equipamento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}