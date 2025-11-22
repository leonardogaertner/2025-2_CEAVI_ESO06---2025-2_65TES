package com.gerenciador.reservas;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.gerenciador.reservas.controller.EquipamentoController;
import com.gerenciador.reservas.controller.ReservaController;
import com.gerenciador.reservas.controller.SalaController;
import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.EquipamentoRepository;
import com.gerenciador.reservas.repository.ReservaRepository;
import com.gerenciador.reservas.repository.SalaRepository;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegracaoSistemaTest {

    @Autowired
    private EquipamentoController equipamentoController;
    @Autowired
    private SalaController salaController;
    @Autowired
    private ReservaController reservaController;
    @Autowired
    private SalaRepository salaRepository;
    @Autowired
    private EquipamentoRepository equipamentoRepository;
    @Autowired
    private ReservaRepository reservaRepository;

    private RedirectAttributes redirectAttrs;
    private Model model;
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        redirectAttrs = new RedirectAttributesModelMap();
        model = new BindingAwareModelMap();
        bindingResult = new BeanPropertyBindingResult(null, "");
    }

    @Test
    @DisplayName("CT01: Deve associar equipamento recém-criado a uma sala")
    void integracao_CT01_EquipamentoParaSala() {
        // Arrange
        Sala sala = new Sala("S01", "Sala Base", 10);
        salaController.salvarSala(sala, bindingResult, redirectAttrs, model);

        Equipamento equip = new Equipamento("Projetor 8K", "Novo modelo");
        equipamentoController.salvarEquipamento(equip, bindingResult, redirectAttrs);

        // Act
        Sala salaParaEditar = salaRepository.findById("S01").orElseThrow();
        Equipamento equipDoBanco = equipamentoRepository.findAll().get(0);
        salaParaEditar.setEquipamentos(List.of(equipDoBanco));
        
        bindingResult = new BeanPropertyBindingResult(salaParaEditar, "sala");
        salaController.salvarSala(salaParaEditar, bindingResult, redirectAttrs, model);

        // Assert Principal: Verificar se a sala no banco possui o equipamento associado
        assertTrue(salaRepository.findById("S01").get()
                .getEquipamentos().stream()
                .anyMatch(e -> e.getNome().equals("Projetor 8K")));
    }

    @Test
    @DisplayName("CT02: Deve criar reserva para sala recém-criada")
    void integracao_CT02_SalaParaReserva() {
        // Arrange
        Sala sala = new Sala("S50", "Sala de Integração", 10);
        bindingResult = new BeanPropertyBindingResult(sala, "sala");
        salaController.salvarSala(sala, bindingResult, redirectAttrs, model);

        // Act
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime fim = inicio.plusHours(2);
        reservaController.processarReserva("S50", "Teste de Integração", inicio, fim, model);

        // Assert Principal: Verificar se a reserva foi persistida corretamente no banco
        assertEquals("Teste de Integração", reservaRepository.findAll().get(0).getNomeResponsavel());
    }

    @Test
    @DisplayName("CT03: Deve falhar ao excluir sala que possui reservas")
    void integracao_CT03_ExcluirSalaComReservas() {
        // Arrange
        Sala sala = new Sala("S01", "Sala com Reserva", 10);
        salaRepository.save(sala);
        Reserva reserva = new Reserva("Responsável", sala, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        reservaRepository.save(reserva);

        // Act
        salaController.apagarSala("S01", redirectAttrs);

        // Assert Principal: A sala ainda deve existir no banco de dados (exclusão impedida)
        assertTrue(salaRepository.existsById("S01"));
    }

    @Test
    @DisplayName("CT04: Deve falhar ao excluir equipamento em uso por uma sala")
    void integracao_CT04_ExcluirEquipamentoEmUso() {
        // Arrange
        Equipamento equip = new Equipamento("Projetor", "Teste");
        equipamentoRepository.save(equip);
        Sala sala = new Sala("S01", "Sala com Equip", 10);
        sala.setEquipamentos(List.of(equip));
        salaRepository.save(sala);
        Long equipId = equip.getId();

        // Act
        equipamentoController.apagarEquipamento(equipId, redirectAttrs);

        // Assert Principal: O equipamento ainda deve existir no banco
        assertTrue(equipamentoRepository.existsById(equipId));
    }

    @Test
    @DisplayName("CT05: Deve falhar ao reservar com conflito de horário (Integração)")
    void integracao_CT05_ConflitoDeReserva() {
        // Arrange
        Sala sala = salaRepository.save(new Sala("S100", "Sala Conflito", 10));
        LocalDateTime inicio = LocalDateTime.of(2025, 11, 20, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 11, 20, 15, 0);
        
        // Primeira reserva (sucesso implícito para o teste de conflito)
        reservaController.processarReserva(sala.getId(), "Ana", inicio, fim, model);

        // Act: Tentar conflito
        LocalDateTime inicioConflito = LocalDateTime.of(2025, 11, 20, 14, 30);
        LocalDateTime fimConflito = LocalDateTime.of(2025, 11, 20, 15, 30);
        Model modelConflito = new BindingAwareModelMap();
        reservaController.processarReserva(sala.getId(), "Bruno", inicioConflito, fimConflito, modelConflito);

        // Assert Principal: Verificar se a mensagem de erro específica de conflito foi gerada
        assertEquals("O horário solicitado para esta sala já está reservado.", 
                modelConflito.getAttribute("mensagemErro"));
    }

    @Test
    @DisplayName("CT06: Deve ATUALIZAR sala com ID utilizado (Integração)")
    void integracao_CT06_SalvarSalaComIdDuplicado() {
        // Arrange
        Sala s1 = new Sala("S_DUP", "Sala Original", 10);
        salaController.salvarSala(s1, bindingResult, redirectAttrs, model);

        // Act: Salvar com mesmo ID e nome diferente
        Sala s2 = new Sala("S_DUP", "Sala Duplicada", 5);
        redirectAttrs.getFlashAttributes().clear();
        salaController.salvarSala(s2, bindingResult, redirectAttrs, model);

        // Assert Principal: O nome da sala no banco deve ter sido atualizado
        assertEquals("Sala Duplicada", salaRepository.findById("S_DUP").get().getNome());
    }

    @Test
    @DisplayName("CT07: Deve falhar ao salvar sala com nome vazio (BindingResult Integração)")
    void integracao_CT07_ValidacaoDeController() {
        // Arrange
        Sala salaInvalida = new Sala("S_VALID", "", 10);
        BindingResult brInvalido = new BeanPropertyBindingResult(salaInvalida, "sala");
        brInvalido.rejectValue("nome", "NotBlank", "O Nome é obrigatório");

        // Act
        String viewName = salaController.salvarSala(salaInvalida, brInvalido, redirectAttrs, model);

        // Assert Principal: Deve retornar para a view do formulário (não redirecionar)
        assertEquals("sala-form", viewName);
    }

    @Test
    @DisplayName("CT08: Deve remover associação de equipamento ao editar sala (Integração)")
    void integracao_CT08_RemoverAssociacao() {
        // Arrange
        Equipamento e1 = equipamentoRepository.save(new Equipamento("Projetor", ""));
        Sala s1 = new Sala("S_EDIT", "Sala com Equipamento", 10);
        s1.setEquipamentos(List.of(e1));
        salaController.salvarSala(s1, bindingResult, redirectAttrs, model);

        // Act: Salvar com lista vazia
        Sala salaSalva = salaRepository.findById("S_EDIT").get();
        salaSalva.setEquipamentos(Collections.emptyList());
        
        BindingResult bindingResultEdit = new BeanPropertyBindingResult(salaSalva, "sala");
        redirectAttrs = new RedirectAttributesModelMap();
        salaController.salvarSala(salaSalva, bindingResultEdit, redirectAttrs, model);

        // Assert Principal: A lista de equipamentos da sala no banco deve estar vazia
        assertTrue(salaRepository.findById("S_EDIT").get().getEquipamentos().isEmpty());
    }

    @Test
    @DisplayName("CT09: Deve exibir salas e equipamentos na página principal (Integração)")
    void integracao_CT09_VisualizacaoDadosNaHome() {
        // Arrange
        Equipamento e1 = equipamentoRepository.save(new Equipamento("Projetor", ""));
        Sala s1 = new Sala("S_HOME", "Sala Home", 10);
        s1.setEquipamentos(List.of(e1));
        salaRepository.save(s1);

        // Act
        Model modelHome = new BindingAwareModelMap();
        reservaController.exibirFormulario(modelHome);

        // Assert Principal: O modelo deve conter a lista de salas
        @SuppressWarnings("unchecked")
        List<Sala> salasDoModel = (List<Sala>) modelHome.getAttribute("salas");
        assertEquals(1, salasDoModel.size());
    }

    @Test
    @DisplayName("CT10: Deve apagar sala com sucesso após remover reservas (Integração)")
    void integracao_CT10_ExclusaoLimpaAposRemoverDependencia() {
        // Arrange
        Sala s1 = salaRepository.save(new Sala("S_DEL", "Sala Delete", 10));
        Reserva r1 = reservaRepository.save(new Reserva("Ana", s1, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        // Act 1 (Tentativa falha ignorada no assert, parte do fluxo)
        salaController.apagarSala(s1.getId(), redirectAttrs);
        
        // Act 2 (Correção e Nova Tentativa)
        reservaRepository.delete(r1);
        redirectAttrs = new RedirectAttributesModelMap();
        salaController.apagarSala(s1.getId(), redirectAttrs);

        // Assert Principal: A sala não deve mais existir no banco
        assertFalse(salaRepository.existsById("S_DEL"));
    }
}