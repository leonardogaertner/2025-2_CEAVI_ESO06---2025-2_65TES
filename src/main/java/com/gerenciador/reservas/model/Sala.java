package com.gerenciador.reservas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import java.util.ArrayList; // Importar ArrayList
import java.util.List;

import jakarta.validation.constraints.NotBlank; // IMPORTAR
import jakarta.validation.constraints.Min; // IMPORTAR
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Sala {

    @Id
    @NotBlank(message = "O ID é obrigatório")
    @Size(min = 3, max = 10, message = "O ID deve ter entre 3 e 10 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 _-]+$", message = "O ID não deve conter caracteres especiais (apenas letras, números, espaços, underscore ou hífen)")
    private String id;

    @NotBlank(message = "O Nome é obrigatório")
    @Size(min = 3, max = 25, message = "O nome deve ter entre 3 e 25 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ _]+$", message = "O nome não deve conter caracteres especiais (apenas letras, números, espaços e underscore)")
    private String nome;

    @Min(value = 1, message = "A capacidade deve ser pelo menos 1")
    private int capacidade;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Equipamento> equipamentos = new ArrayList<>();
    public Sala() {
    }

    public Sala(String id, String nome, int capacidade) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.equipamentos = new ArrayList<>();
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

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public List<Equipamento> getEquipamentos() {
        return equipamentos;
    }

    public void setEquipamentos(List<Equipamento> equipamentos) {
        this.equipamentos = equipamentos;
    }

    public void adicionarEquipamento(Equipamento equipamento) {
        if (this.equipamentos == null) {
            this.equipamentos = new ArrayList<>();
        }
        this.equipamentos.add(equipamento);
    }
}