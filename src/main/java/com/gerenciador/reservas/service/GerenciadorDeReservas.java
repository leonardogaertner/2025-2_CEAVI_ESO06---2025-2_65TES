package com.gerenciador.reservas.service;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.ReservaRepository;
import com.gerenciador.reservas.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GerenciadorDeReservas {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private SalaRepository salaRepository;

    // MÉTODO ATUALIZADO: Recebe 'nomeResponsavel' como String
    public Reserva reservarSala(Sala sala, String nomeResponsavel, LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data de fim da reserva não pode ser anterior à data de início.");
        }

        List<Reserva> conflitos = reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(sala, dataInicio,
                dataFim);

        if (!conflitos.isEmpty()) {
            throw new IllegalStateException("O horário solicitado para esta sala já está reservado.");
        }

        // CRIA A RESERVA COM A STRING
        Reserva novaReserva = new Reserva(nomeResponsavel, sala, dataInicio, dataFim);

        return reservaRepository.save(novaReserva);
    }

    public List<Reserva> getReservas() {
        return reservaRepository.findAll();
    }

    public List<Sala> getSalas() {
        return salaRepository.findAll();
    }

    public Sala getSalaById(String id) {
        return salaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sala não encontrada!"));
    }
}