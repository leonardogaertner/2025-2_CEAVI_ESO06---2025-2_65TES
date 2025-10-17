package com.gerenciador.reservas.repository;

import com.gerenciador.reservas.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    // Esta interface herda todos os métodos CRUD básicos do JpaRepository,
    // como findAll(), findById(), save(), e deleteById().
    // O Spring implementa estes métodos automaticamente em tempo de execução.
}