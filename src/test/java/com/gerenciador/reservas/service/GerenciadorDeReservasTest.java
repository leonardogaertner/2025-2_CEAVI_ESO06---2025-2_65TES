package com.gerenciador.reservas.service;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.ReservaRepository;
import com.gerenciador.reservas.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
// 1. Importa a extensão do Mockito para JUnit 5
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 2. Diz ao JUnit para usar o Mockito
@ExtendWith(MockitoExtension.class)
class GerenciadorDeReservasTest {

    // 3. Cria a instância real do Service e injeta os mocks nele
    @InjectMocks
    private GerenciadorDeReservas gerenciador;

    // 4. Cria os mocks para as dependências do Service
    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalaRepository salaRepository;

    // Variáveis de setup
    private Sala sala;
    private LocalDateTime inicio;
    private LocalDateTime fim;

    @BeforeEach
    void setUp() {
        // Prepara os dados base para os testes
        sala = new Sala("S01", "Sala de Teste", 10);
        // Datas base do plano de testes: 21/10/2025, 14:00 às 15:00
        inicio = LocalDateTime.of(2025, 10, 21, 14, 0, 0);
        fim = LocalDateTime.of(2025, 10, 21, 15, 0, 0);
    }

    @Test
    @DisplayName("CT14: Deve reservar sala com sucesso (Caminho Feliz)")
    void reservarSala_CT14_CaminhoFeliz() {
        // Cenário (Arrange)
        // "Quando o repositório for chamado, retorne uma lista vazia (sem conflitos)."
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(sala, inicio, fim))
                .thenReturn(Collections.emptyList());

        // "Quando o método save for chamado, apenas retorne o objeto."
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ação (Act)
        // Executamos o método que queremos testar
        Reserva novaReserva = gerenciador.reservarSala(sala, "Ana", inicio, fim);

        // Verificação (Assert)
        assertNotNull(novaReserva);
        assertEquals("Ana", novaReserva.getNomeResponsavel());
        assertEquals(sala, novaReserva.getSala());
        // Verificamos se o método save() do Mock foi realmente chamado 1 vez.
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    @DisplayName("CT15: Deve falhar se data de fim for anterior à de início")
    void reservarSala_CT15_DataFimAntesDeInicio() {
        // Cenário (Arrange)
        LocalDateTime dataInicioInvertida = LocalDateTime.of(2025, 10, 21, 17, 0);
        LocalDateTime dataFimInvertida = LocalDateTime.of(2025, 10, 21, 16, 0);

        // Ação (Act) & Verificação (Assert)
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gerenciador.reservarSala(sala, "Carla", dataInicioInvertida, dataFimInvertida);
        });

        // Verificamos a mensagem de erro exata do plano de testes
        assertEquals("A data de fim da reserva não pode ser anterior à data de início.", exception.getMessage());
    }

    @Test
    @DisplayName("CT17: Deve falhar por conflito (sobreposição parcial)")
    void reservarSala_CT17_ConflitoParcial() {
        // Cenário (Arrange)
        Reserva reservaExistente = new Reserva("Reserva Base", sala, inicio, fim);
        // Dados da nova tentativa de reserva (CT17)
        LocalDateTime novoInicio = LocalDateTime.of(2025, 10, 21, 14, 30);
        LocalDateTime novoFim = LocalDateTime.of(2025, 10, 21, 15, 30);

        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(sala, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        // Ação (Act) & Verificação (Assert)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(sala, "Bruno", novoInicio, novoFim);
        });

        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }

    @Test
    @DisplayName("CT18/CT19: Deve permitir reservas contíguas (Testes de Limite)")
    void reservarSala_CT18_CT19_HorariosContiguos() {
        // Cenário (Arrange)
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Ação (Act) & Verificação (Assert)

        // CT18: Reserva termina EXATAMENTE quando a outra começa (13:00 - 14:00)
        assertDoesNotThrow(() -> {
            gerenciador.reservarSala(sala, "Diego", inicio.minusHours(1), inicio);
        });

        // CT19: Reserva começa EXATAMENTE quando a outra termina (15:00 - 16:00)
        assertDoesNotThrow(() -> {
            gerenciador.reservarSala(sala, "Elisa", fim, fim.plusHours(1));
        });
    }

    @Test
    @DisplayName("CT20: Deve falhar por conflito (reserva 'envelopa' existente)")
    void reservarSala_CT20_ConflitoEnvelope() {
        // Cenário (Arrange)
        Reserva reservaExistente = new Reserva("Reserva Base", sala, inicio, fim); // 14:00 - 15:00
        LocalDateTime novoInicio = LocalDateTime.of(2025, 10, 21, 13, 0); // 13:00
        LocalDateTime novoFim = LocalDateTime.of(2025, 10, 21, 16, 0); // 16:00

        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(sala, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        // Ação (Act) & Verificação (Assert)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(sala, "Fábio", novoInicio, novoFim);
        });
        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }

    @Test
    @DisplayName("CT21: Deve falhar por conflito (reserva 'dentro' de existente)")
    void reservarSala_CT21_ConflitoDentro() {
        // Cenário (Arrange)
        Reserva reservaExistente = new Reserva("Reserva Base", sala, inicio, fim); // 14:00 - 15:00
        LocalDateTime novoInicio = LocalDateTime.of(2025, 10, 21, 14, 15); // 14:15
        LocalDateTime novoFim = LocalDateTime.of(2025, 10, 21, 14, 45); // 14:45

        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(sala, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        // Ação (Act) & Verificação (Assert)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(sala, "Gabriela", novoInicio, novoFim);
        });
        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }
}