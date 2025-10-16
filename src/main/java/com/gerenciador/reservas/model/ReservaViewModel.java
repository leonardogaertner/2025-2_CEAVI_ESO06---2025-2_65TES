package com.gerenciador.reservas.model;

import java.time.LocalDateTime;

/**
 * Uma classe simples para carregar os dados de uma reserva já formatados para a
 * view.
 */
public class ReservaViewModel {
    private String nomeResponsavel;
    private String nomeSala;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public ReservaViewModel(String nomeResponsavel, String nomeSala, LocalDateTime dataInicio, LocalDateTime dataFim) {
        this.nomeResponsavel = nomeResponsavel;
        this.nomeSala = nomeSala;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // Getters para o Thymeleaf acessar os dados
    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public String getNomeSala() {
        return nomeSala;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }
}