package com.gerenciador.reservas.repository;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Spring Data JPA é inteligente! Ele cria a query automaticamente a partir do
    // nome do método.
    // Esta query buscará todas as reservas para uma sala específica que conflitem
    // com um determinado intervalo de tempo.
    List<Reserva> findBySalaAndDataFimAfterAndDataInicioBefore(Sala sala, LocalDateTime dataInicio,
            LocalDateTime dataFim);
}