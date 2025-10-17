package com.gerenciador.reservas.viewModel;

import java.time.LocalDateTime;

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