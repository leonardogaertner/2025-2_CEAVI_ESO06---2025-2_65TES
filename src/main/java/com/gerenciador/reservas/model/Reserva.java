package com.gerenciador.reservas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idReserva;

    // VOLTOU A SER UMA STRING SIMPLES
    private String nomeResponsavel;

    @ManyToOne
    private Sala sala;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    public Reserva() {
    }

    // CONSTRUTOR ATUALIZADO
    public Reserva(String nomeResponsavel, Sala sala, LocalDateTime dataInicio, LocalDateTime dataFim) {
        this.nomeResponsavel = nomeResponsavel;
        this.sala = sala;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    // GETTERS E SETTERS ATUALIZADOS
    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
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