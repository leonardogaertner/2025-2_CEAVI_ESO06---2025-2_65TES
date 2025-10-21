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

@Entity
public class Sala {

    @Id
    @NotBlank(message = "O ID é obrigatório")
    private String id;

    @NotBlank(message = "O Nome é obrigatório")
    private String nome;

    @Min(value = 1, message = "A capacidade deve ser pelo menos 1")
    private int capacidade;

    // A LINHA ABAIXO É A CORREÇÃO:
    // Inicializamos a lista aqui para garantir que ela nunca seja nula.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Equipamento> equipamentos = new ArrayList<>();

    // Construtor padrão agora não precisa mais inicializar a lista
    public Sala() {
    }

    public Sala(String id, String nome, int capacidade) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        // A lista já é inicializada na declaração, mas podemos garantir aqui também se
        // quisermos
        this.equipamentos = new ArrayList<>();
    }

    // --- Getters, Setters e métodos auxiliares (permanecem os mesmos) ---

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
        // Esta verificação não é mais estritamente necessária, mas é uma boa prática
        if (this.equipamentos == null) {
            this.equipamentos = new ArrayList<>();
        }
        this.equipamentos.add(equipamento);
    }
}