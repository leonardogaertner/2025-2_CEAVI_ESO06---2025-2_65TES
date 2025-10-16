package com.gerenciador.reservas.model;

import java.time.LocalDateTime;

/**
 * Representa uma reserva de uma sala por um período de tempo.
 * Utiliza LocalDateTime para marcar o início e o fim com precisão de data e
 * hora.
 */
public class Reserva {

    private String idReserva;
    private String idSala; // Para vincular à sala reservada
    private String nomeResponsavel;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    // Construtor
    public Reserva(String idReserva, String idSala, String nomeResponsavel, LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        this.idReserva = idReserva;
        this.idSala = idSala;
        this.nomeResponsavel = nomeResponsavel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // Getters e Setters
    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getIdSala() {
        return idSala;
    }

    public void setIdSala(String idSala) {
        this.idSala = idSala;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }
}