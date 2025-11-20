package com.gerenciador.reservas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity // Define que esta classe é uma tabela no banco de dados
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "O Nome é obrigatório")
    @Size(min = 3, max = 20, message = "O nome deve ter entre 3 e 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ _]+$", message = "O nome não deve conter caracteres especiais (apenas letras, números, espaços e underscore)")
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