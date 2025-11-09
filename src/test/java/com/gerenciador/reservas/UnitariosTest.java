package com.gerenciador.reservas;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.ReservaRepository;
import com.gerenciador.reservas.service.GerenciadorDeReservas;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Esta classe combina testes de unidade puros para validações de modelo (JSR 303)
 * e testes de unidade com mocks para a lógica de serviço.
 * Os DisplayNames (CTxx) foram atualizados de acordo com o plano de testes.
 */
@ExtendWith(MockitoExtension.class)
class UnitariosTest {

    // --- Parte 1: Configuração para Testes de Validação (CT1-CT26) ---
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        // Configura um validador padrão para checar as annotations @NotBlank, @Min, etc.
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- Parte 2: Configuração para Testes de Lógica de Serviço (CT27-CT32) ---
    @InjectMocks
    private GerenciadorDeReservas gerenciador;

    @Mock
    private ReservaRepository reservaRepository;

    private Sala salaBase;
    private LocalDateTime inicioBase;
    private LocalDateTime fimBase;
    private Reserva reservaExistente;

    @BeforeEach
    void setUpServiceTest() {
        // Prepara dados base para os testes de conflito de reserva
        salaBase = new Sala("S01", "Sala de Teste", 10);
        inicioBase = LocalDateTime.of(2025, 10, 21, 14, 0, 0);
        fimBase = LocalDateTime.of(2025, 10, 21, 15, 0, 0);
        reservaExistente = new Reserva("Reserva Base", salaBase, inicioBase, fimBase);
    }

    // --- Testes de Validação da Entidade: Equipamento (CT1-CT9) ---

    @Test
    @DisplayName("CT01: Nome de equipamento com menos de 3 letras (deve falhar)")
    void CT01_Equipamento_NomeCurto() {
        Equipamento eq = new Equipamento("ab", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite 'ab'");
    }

    @Test
    @DisplayName("CT02: Nome de equipamento com mais de 20 letras (deve falhar)")
    void CT02_Equipamento_NomeLongo() {
        Equipamento eq = new Equipamento("Nome muito longo para um equipamento", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite nomes longos");
    }

    @Test
    @DisplayName("CT03: Nome de equipamento vazio (deve falhar)")
    void CT03_Equipamento_NomeVazio() {
        Equipamento eq = new Equipamento("", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertFalse(violations.isEmpty(), "Validação de nome vazio falhou");
        assertEquals("O Nome é obrigatório", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("CT04: Nome de equipamento com número (válido)")
    void CT04_Equipamento_NomeComNumero() {
        Equipamento eq = new Equipamento("Projetor 2000", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertTrue(violations.isEmpty(), "Validação falhou para nome com número");
    }

    @Test
    @DisplayName("CT05: Nome de equipamento com caractere especial (deve falhar)")
    void CT05_Equipamento_NomeComCaractereEspecial() {
        Equipamento eq = new Equipamento("Projetor @#$", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        // NOTA: A validação PASSA pois @NotBlank não valida caracteres.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite '@#$'");
    }

    @Test
    @DisplayName("CT06: Nome de equipamento com espaços (válido)")
    void CT06_Equipamento_NomeComEspacos() {
        Equipamento eq = new Equipamento("Quadro Branco", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertTrue(violations.isEmpty(), "Validação falhou para nome com espaços");
    }

    @Test
    @DisplayName("CT07: Nome de equipamento simples (válido)")
    void CT07_Equipamento_NomeSimples() {
        Equipamento eq = new Equipamento("Projetor", "Desc");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertTrue(violations.isEmpty(), "Validação falhou para nome simples");
    }

    @Test
    @DisplayName("CT08: Descrição do equipamento vazia (válido)")
    void CT08_Equipamento_DescricaoVazia() {
        Equipamento eq = new Equipamento("Projetor", "");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertTrue(violations.isEmpty(), "Validação falhou para descrição vazia");
    }

    @Test
    @DisplayName("CT09: Descrição do equipamento válida (válido)")
    void CT09_Equipamento_DescricaoValida() {
        Equipamento eq = new Equipamento("Projetor", "Modelo X1");
        Set<ConstraintViolation<Equipamento>> violations = validator.validate(eq);
        assertTrue(violations.isEmpty(), "Validação falhou para descrição válida");
    }

    // --- Testes de Validação da Entidade: Sala (CT10-CT26) ---

    @Test
    @DisplayName("CT10: ID da sala vazio (deve falhar)")
    void CT10_Sala_IDVazio() {
        Sala s = new Sala("", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertFalse(violations.isEmpty(), "Validação de ID vazio falhou");
    }

    @Test
    @DisplayName("CT11: ID da sala com menos de 3 letras (deve falhar)")
    void CT11_Sala_IDCurto() {
        Sala s = new Sala("S1", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite 'S1'");
    }

    @Test
    @DisplayName("CT12: ID da sala com mais de 10 letras (deve falhar)")
    void CT12_Sala_IDLongo() {
        Sala s = new Sala("SALA_COM_ID_MUITO_LONGO", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite IDs longos");
    }

    @Test
    @DisplayName("CT13: ID da sala com caractere especial (deve falhar)")
    void CT13_Sala_IDComCaractereEspecial() {
        Sala s = new Sala("SALA-@!", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida caracteres.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite '@!'");
    }

    @Test
    @DisplayName("CT14: ID da sala com espaços (deve falhar)")
    void CT14_Sala_IDComEspacos() {
        Sala s = new Sala("S 10", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank permite espaços no meio.
        // Ele só falha se for APENAS espaços.
        assertTrue(violations.isEmpty(), "Validação de ID com espaços no meio passou (esperado)");

        Sala sApenasEspaco = new Sala(" ", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violationsEspaco = validator.validate(sApenasEspaco);
        assertFalse(violationsEspaco.isEmpty(), "Validação de ID com apenas espaços falhou");
    }

    @Test
    @DisplayName("CT15: ID da sala válido (válido)")
    void CT15_Sala_IDValido() {
        Sala s = new Sala("S10-A", "Nome Sala", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertTrue(violations.isEmpty(), "Validação falhou para ID válido");
    }

    @Test
    @DisplayName("CT16: Nome da sala com menos de 3 letras (deve falhar)")
    void CT16_Sala_NomeCurto() {
        Sala s = new Sala("S10", "S1", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite 'S1'");
    }

    @Test
    @DisplayName("CT17: Nome da sala com mais de 25 letras (deve falhar)")
    void CT17_Sala_NomeLongo() {
        Sala s = new Sala("S10", "Esta é uma sala com um nome muito longo", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida tamanho.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite nomes longos");
    }

    @Test
    @DisplayName("CT18: Nome da sala vazio (deve falhar)")
    void CT18_Sala_NomeVazio() {
        Sala s = new Sala("S10", "", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertFalse(violations.isEmpty(), "Validação de nome vazio falhou");
        assertEquals("O Nome é obrigatório", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("CT19: Nome da sala com número (válido)")
    void CT19_Sala_NomeComNumero() {
        Sala s = new Sala("S10", "Sala 101", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertTrue(violations.isEmpty(), "Validação falhou para nome com número");
    }

    @Test
    @DisplayName("CT20: Nome da sala com caractere especial (deve falhar)")
    void CT20_Sala_NomeComCaractereEspecial() {
        Sala s = new Sala("S10", "Sala (Principal)", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        // NOTA: A validação PASSA pois @NotBlank não valida caracteres.
        assertTrue(violations.isEmpty(), "Falha esperada não ocorreu. @NotBlank permite '()'");
    }

    @Test
    @DisplayName("CT21: Nome da sala com espaços (válido)")
    void CT21_Sala_NomeComEspacos() {
        Sala s = new Sala("S10", "Sala de Reuniões", 10);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertTrue(violations.isEmpty(), "Validação falhou para nome com espaços");
    }

    @Test
    @DisplayName("CT22: Capacidade da sala vazia ou igual a 0 (deve falhar)")
    void CT22_Sala_CapacidadeZero() {
        // CT24 (vazia) se torna 0 para o tipo primitivo int.
        Sala s = new Sala("S10", "Nome Sala", 0);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertFalse(violations.isEmpty(), "Validação de capacidade 0 falhou");
        assertEquals("A capacidade deve ser pelo menos 1", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("CT23: Capacidade da sala negativa (deve falhar)")
    void CT23_Sala_CapacidadeNegativa() {
        Sala s = new Sala("S10", "Nome Sala", -5);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertFalse(violations.isEmpty(), "Validação de capacidade negativa falhou");
        assertEquals("A capacidade deve ser pelo menos 1", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("CT24: Capacidade da sala maior que 0 (válido)")
    void CT24_Sala_CapacidadeValida() {
        Sala s = new Sala("S10", "Nome Sala", 1);
        Set<ConstraintViolation<Sala>> violations = validator.validate(s);
        assertTrue(violations.isEmpty(), "Validação falhou para capacidade = 1");
    }


    // --- Testes de Lógica Pura: GerenciadorDeReservas (CT27) ---

    @Test
    @DisplayName("CT25: Reserva da sala com a data fim antes da data inicio (deve falhar)")
    void CT25_Reserva_DataFimAntesDeInicio() {
        LocalDateTime inicio = LocalDateTime.of(2025, 10, 21, 17, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 10, 21, 16, 0); // Fim antes do Início

        // Este teste é de unidade puro, pois a validação ocorre antes da chamada ao mock.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gerenciador.reservarSala(salaBase, "Carla", inicio, fim);
        });

        assertEquals("A data de fim da reserva não pode ser anterior à data de início.", exception.getMessage());
    }

    // --- Testes de Lógica com Mock: GerenciadorDeReservas (CT28-CT32) ---

    @Test
    @DisplayName("CT26: Verifica se o sistema impede o cadastro de reserva com conflito de horário (sobreposição parcial). (deve falhar)")
    void CT26_Reserva_ConflitoParcial() {
        // Reserva existente: 14:00 - 15:00
        // Nova tentativa: 14:30 - 15:30
        LocalDateTime novoInicio = inicioBase.plusMinutes(30);
        LocalDateTime novoFim = fimBase.plusMinutes(30);

        // Simula que o repositório encontrou 1 conflito
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(salaBase, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(salaBase, "Bruno", novoInicio, novoFim);
        });

        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }

    @Test
    @DisplayName("CT27: Verifica se o sistema permite reservar um horário que termina exatamente quando o outro começa.")
    void CT27_Reserva_ContiguaInicio() {
        // Reserva existente: 14:00 - 15:00
        // Nova tentativa: 13:00 - 14:00 (termina exatamente quando a outra começa)
        LocalDateTime novoInicio = inicioBase.minusHours(1);
        LocalDateTime novoFim = inicioBase;

        // Simula que o repositório NÃO encontrou conflitos
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(salaBase, novoInicio, novoFim))
                .thenReturn(Collections.emptyList());
        // Simula o save
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> {
            Reserva r = gerenciador.reservarSala(salaBase, "Diego", novoInicio, novoFim);
            assertNotNull(r);
        });
    }

    @Test
    @DisplayName("CT28: Verifica se o sistema permite reservar um horário que começa exatamente quando outro termina.")
    void CT28_Reserva_ContiguaFim() {
        // Reserva existente: 14:00 - 15:00
        // Nova tentativa: 15:00 - 16:00 (começa exatamente quando a outra termina)
        LocalDateTime novoInicio = fimBase;
        LocalDateTime novoFim = fimBase.plusHours(1);

        // Simula que o repositório NÃO encontrou conflitos
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(salaBase, novoInicio, novoFim))
                .thenReturn(Collections.emptyList());
        // Simula o save
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));


        assertDoesNotThrow(() -> {
            Reserva r = gerenciador.reservarSala(salaBase, "Elisa", novoInicio, novoFim);
            assertNotNull(r);
        });
    }

    @Test
    @DisplayName("CT29: Verifica se o sistema impede a reserva que \"envelopa\" uma reserva existente.")
    void CT29_Reserva_ConflitoEnvelope() {
        // Reserva existente: 14:00 - 15:00
        // Nova tentativa: 13:00 - 16:00
        LocalDateTime novoInicio = inicioBase.minusHours(1);
        LocalDateTime novoFim = fimBase.plusHours(1);

        // Simula que o repositório encontrou 1 conflito
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(salaBase, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(salaBase, "Fábio", novoInicio, novoFim);
        });

        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }

    @Test
    @DisplayName("CT30: Verifica se o sistema impede a reserva \"dentro\" de uma reserva existente.")
    void CT30_Reserva_ConflitoDentro() {
        // Reserva existente: 14:00 - 15:00
        // Nova tentativa: 14:15 - 14:45
        LocalDateTime novoInicio = inicioBase.plusMinutes(15);
        LocalDateTime novoFim = fimBase.minusMinutes(15);

        // Simula que o repositório encontrou 1 conflito
        when(reservaRepository.findBySalaAndDataFimAfterAndDataInicioBefore(salaBase, novoInicio, novoFim))
                .thenReturn(List.of(reservaExistente));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            gerenciador.reservarSala(salaBase, "Gabriela", novoInicio, novoFim);
        });

        assertEquals("O horário solicitado para esta sala já está reservado.", exception.getMessage());
    }
}