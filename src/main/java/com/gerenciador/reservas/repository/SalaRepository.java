package com.gerenciador.reservas.repository;

import com.gerenciador.reservas.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaRepository extends JpaRepository<Sala, String> {
    // O JpaRepository já nos dá métodos como findAll(), findById(), save(),
    // deleteById(), etc.
    // O primeiro parâmetro é a Entidade (Sala) e o segundo é o tipo da Chave
    // Primária (String).
}