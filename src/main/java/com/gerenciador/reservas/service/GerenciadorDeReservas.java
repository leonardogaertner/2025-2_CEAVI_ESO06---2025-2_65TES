package com.gerenciador.reservas.service;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Classe central que contém a lógica de negócio para gerenciar reservas de
 * salas.
 */
public class GerenciadorDeReservas {

    private final List<Sala> salas = new ArrayList<>();
    private final List<Reserva> reservas = new ArrayList<>();

    /**
     * Lógica principal para criar uma nova reserva.
     * Valida as regras de negócio antes de efetivar a reserva.
     */
    public Reserva reservarSala(String idSala, String nomeResponsavel, LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        // 1. Validação de Regra de Negócio: a data de fim não pode ser anterior à data
        // de início.
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim da reserva não pode ser anterior à data de início.");
        }

        // 2. Filtra as reservas existentes apenas para a sala de interesse.
        List<Reserva> reservasDaSala = reservas.stream()
                .filter(reserva -> reserva.getIdSala().equals(idSala))
                .collect(Collectors.toList());

        // 3. Verifica se a nova reserva conflita com alguma existente na mesma sala.
        for (Reserva existente : reservasDaSala) {
            boolean haConflito = dataInicio.isBefore(existente.getDataFim())
                    && dataFim.isAfter(existente.getDataInicio());
            if (haConflito) {
                // AJUSTE: Trocamos a exceção customizada pela padrão do Java
                // "IllegalStateException".
                throw new IllegalStateException("O horário solicitado para esta sala já está reservado.");
            }
        }

        // 4. Se todas as validações passarem, cria e armazena a nova reserva.
        String idReserva = UUID.randomUUID().toString(); // Gera um ID único para a reserva
        Reserva novaReserva = new Reserva(idReserva, idSala, nomeResponsavel, dataInicio, dataFim);
        reservas.add(novaReserva);

        System.out.println("Reserva efetuada com sucesso! ID: " + idReserva);
        return novaReserva;
    }

    // Métodos auxiliares para testes e futuras funcionalidades
    public List<Reserva> getReservas() {
        return new ArrayList<>(reservas); // Retorna uma cópia para proteger a lista original
    }

    public void adicionarSala(Sala sala) {
        this.salas.add(sala);
    }
}