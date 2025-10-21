package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.service.GerenciadorDeReservas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @InjectMocks
    private ReservaController controller;

    @Mock
    private GerenciadorDeReservas gerenciador;

    private Sala sala;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private Model model;

    @BeforeEach
    void setUp() {
        sala = new Sala("S01", "Sala Teste", 10);
        inicio = LocalDateTime.of(2025, 10, 21, 14, 0);
        fim = LocalDateTime.of(2025, 10, 21, 15, 0);
        model = new BindingAwareModelMap();

        when(gerenciador.getReservas()).thenReturn(Collections.emptyList());
        when(gerenciador.getSalas()).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("RT09/CT14: Deve processar reserva com sucesso (Caminho Feliz)")
    void processarReserva_CT14_CaminhoFeliz() {
        // Cenário (Arrange)
        when(gerenciador.getSalaById("S01")).thenReturn(sala);
        when(gerenciador.reservarSala(sala, "Ana", inicio, fim))
                .thenReturn(new Reserva("Ana", sala, inicio, fim));

        // Ação (Act)
        String viewName = controller.processarReserva("S01", "Ana", inicio, fim, model);

        // Verificação (Assert)
        assertEquals("Reserva realizada com sucesso!", model.getAttribute("mensagemSucesso"));
        verify(gerenciador).reservarSala(sala, "Ana", inicio, fim);
        assertEquals("index", viewName);
    }

    @Test
    @DisplayName("RT09/CT16: Deve falhar se a sala não for encontrada")
    void processarReserva_CT16_SalaInexistente() {
        // Cenário (Arrange)
        when(gerenciador.getSalaById("S99"))
                .thenThrow(new IllegalArgumentException("Sala não encontrada!"));

        // Ação (Act)
        String viewName = controller.processarReserva("S99", "Hugo", inicio, fim, model);

        // Verificação (Assert)
        assertEquals("Sala não encontrada!", model.getAttribute("mensagemErro"));
        verify(gerenciador, never()).reservarSala(any(), any(), any(), any());
        assertEquals("index", viewName);
    }

    @Test
    @DisplayName("RT10/CT17: Deve falhar se houver conflito de horário")
    void processarReserva_CT17_ConflitoHorario() {
        // Cenário (Arrange)
        String msgErro = "O horário solicitado para esta sala já está reservado.";

        when(gerenciador.getSalaById("S01")).thenReturn(sala);
        when(gerenciador.reservarSala(any(Sala.class), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new IllegalStateException(msgErro));

        // Ação (Act)
        String viewName = controller.processarReserva("S01", "Bruno", inicio, fim, model);

        // Verificação (Assert)
        assertEquals(msgErro, model.getAttribute("mensagemErro"));
        assertEquals("index", viewName);
    }
}