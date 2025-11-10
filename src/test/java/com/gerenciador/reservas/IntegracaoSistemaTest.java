package com.gerenciador.reservas;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private BindingResult bindingResult; // Usado apenas para chamadas de método

    @BeforeEach
    void setUp() {
        redirectAttrs = new RedirectAttributesModelMap();
        model = new BindingAwareModelMap();
        // Cria um BindingResult vazio (sem erros) para passar aos métodos
        bindingResult = new BeanPropertyBindingResult(null, "");
    }

    // --- SEUS 4 TESTES ORIGINAIS ---

    @Test
    @DisplayName("CT01: Deve associar equipamento recém-criado a uma sala")
    void integracao_CT01_EquipamentoParaSala() {
        // Arrange
        Sala sala = new Sala("S01", "Sala Base", 10);
        salaController.salvarSala(sala, bindingResult, redirectAttrs, model); // Salva sala inicial

        Equipamento equip = new Equipamento("Projetor 8K", "Novo modelo");
        equipamentoController.salvarEquipamento(equip, bindingResult, redirectAttrs); // Salva equipamento

        // Act
        Sala salaParaEditar = salaRepository.findById("S01").orElseThrow();
        Equipamento equipDoBanco = equipamentoRepository.findAll().get(0); // Pega o equipamento salvo

        salaParaEditar.setEquipamentos(List.of(equipDoBanco));
        // Recria o BindingResult para a sala (necessário para o método salvar)
        bindingResult = new BeanPropertyBindingResult(salaParaEditar, "sala");
        salaController.salvarSala(salaParaEditar, bindingResult, redirectAttrs, model); // Salva a associação

        // Assert
        Sala salaAtualizada = salaRepository.findById("S01").orElseThrow();
        assertEquals(1, salaAtualizada.getEquipamentos().size());
        assertEquals("Projetor 8K", salaAtualizada.getEquipamentos().get(0).getNome());
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("CT02: Deve criar reserva para sala recém-criada")
    void integracao_CT02_SalaParaReserva() {
        // Arrange
        Sala sala = new Sala("S50", "Sala de Integração", 10);
        bindingResult = new BeanPropertyBindingResult(sala, "sala");
        salaController.salvarSala(sala, bindingResult, redirectAttrs, model); // Salva a sala

        // Act
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withNano(0); // Remove nanossegundos para evitar
                                                                            // problemas de comparação
        LocalDateTime fim = inicio.plusHours(2);
        reservaController.processarReserva("S50", "Teste de Integração", inicio, fim, model); // Cria a reserva

        // Assert
        assertEquals("Reserva realizada com sucesso!", model.getAttribute("mensagemSucesso"));
        assertEquals(1, reservaRepository.count());
        Reserva reservaDoBanco = reservaRepository.findAll().get(0);
        assertEquals("Teste de Integração", reservaDoBanco.getNomeResponsavel());
        assertEquals("S50", reservaDoBanco.getSala().getId());
    }

    @Test
    @DisplayName("CT03: Deve falhar ao excluir sala que possui reservas")
    void integracao_CT03_ExcluirSalaComReservas() {
        // Arrange
        Sala sala = new Sala("S01", "Sala com Reserva", 10);
        salaRepository.save(sala);

        Reserva reserva = new Reserva("Responsável", sala, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        reservaRepository.save(reserva);

        // Act
        // Chama o método do CONTROLLER agora
        String viewName = salaController.apagarSala("S01", redirectAttrs);

        // Assert
        assertEquals("redirect:/salas", viewName); // Verifica o redirecionamento
        // Verifica a mensagem de ERRO (definida no catch do controller)
        assertEquals(
                "Não é possível excluir esta sala, pois ela possui reservas ativas ou está associada a outros dados.",
                redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso")); // Garante que não há msg de sucesso

        // Verifica se a sala e a reserva ainda existem no banco
        assertEquals(1, salaRepository.count());
        assertEquals(1, reservaRepository.count());
    }

    @Test
    @DisplayName("CT04: Deve falhar ao excluir equipamento em uso por uma sala")
    void integracao_CT04_ExcluirEquipamentoEmUso() {
        // Arrange
        Equipamento equip = new Equipamento("Projetor", "Teste");
        equipamentoRepository.save(equip); // Salva o equipamento

        Sala sala = new Sala("S01", "Sala com Equip", 10);
        sala.setEquipamentos(List.of(equip));
        salaRepository.save(sala); // Salva a sala associada ao equipamento

        Long equipId = equip.getId(); // Guarda o ID antes de tentar apagar

        // Act
        // Chama o método do CONTROLLER agora
        String viewName = equipamentoController.apagarEquipamento(equipId, redirectAttrs);

        // Assert
        assertEquals("redirect:/equipamentos", viewName); // Verifica o redirecionamento
        // Verifica a mensagem de ERRO (definida no catch do controller)
        assertEquals("Este equipamento não pode ser excluído, pois está associado a uma ou mais salas.",
                redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso"));

        // Verifica se o equipamento e a sala ainda existem
        assertEquals(1, equipamentoRepository.count());
        assertEquals(1, salaRepository.count());
    }

    // --- NOVOS 6 TESTES DE INTEGRAÇÃO (CT05-CT10) ---

    @Test
    @DisplayName("CT05: Deve falhar ao reservar com conflito de horário (Integração)")
    void integracao_CT05_ConflitoDeReserva() {
        // Arrange
        Sala sala = salaRepository.save(new Sala("S100", "Sala Conflito", 10));
        LocalDateTime inicio = LocalDateTime.of(2025, 11, 20, 14, 0);
        LocalDateTime fim = LocalDateTime.of(2025, 11, 20, 15, 0);

        // Act 1: Fazer a primeira reserva (caminho feliz)
        reservaController.processarReserva(sala.getId(), "Ana", inicio, fim, model);

        // Assert 1
        assertEquals("Reserva realizada com sucesso!", model.getAttribute("mensagemSucesso"));

        // Act 2: Tentar reservar um horário conflitante
        LocalDateTime inicioConflito = LocalDateTime.of(2025, 11, 20, 14, 30);
        LocalDateTime fimConflito = LocalDateTime.of(2025, 11, 20, 15, 30);
        // Novo model para capturar a resposta da segunda chamada
        Model modelConflito = new BindingAwareModelMap();
        reservaController.processarReserva(sala.getId(), "Bruno", inicioConflito, fimConflito, modelConflito);

        // Assert 2
        assertEquals("O horário solicitado para esta sala já está reservado.", modelConflito.getAttribute("mensagemErro"));
        assertEquals(1, reservaRepository.count()); // Apenas a primeira reserva deve existir
    }

    @Test
    @DisplayName("CT06: Deve ATUALIZAR sala com ID duplicado (Integração)")
    void integracao_CT06_SalvarSalaComIdDuplicado() {
        // Arrange
        Sala s1 = new Sala("S_DUP", "Sala Original", 10);
        
        // Act 1: Salvar a primeira sala
        String viewName1 = salaController.salvarSala(s1, bindingResult, redirectAttrs, model);

        // Assert 1
        assertEquals("redirect:/salas", viewName1);
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        assertEquals(1, salaRepository.count()); // Garante que 1 sala foi criada

        // Arrange 2
        Sala s2 = new Sala("S_DUP", "Sala Duplicada", 5); // Mesmo ID, nome diferente
        // Limpa os atributos de redirecionamento para a segunda chamada
        redirectAttrs.getFlashAttributes().clear(); // Correção: usar .clear() em vez de reatribuir
        
        // Act 2: Tentar salvar a segunda sala com mesmo ID (isso irá ATUALIZAR)
        String viewName2 = salaController.salvarSala(s2, bindingResult, redirectAttrs, model);

        // Assert 2 (CORRIGIDO)
        assertEquals("redirect:/salas", viewName2);
        
        // Verifique se a ATUALIZAÇÃO foi bem-sucedida
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        
        // Verifique se a mensagem de ERRO está (corretamente) nula
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemErro"));
        
        // Verifique se o número de salas no banco AINDA é 1
        assertEquals(1, salaRepository.count()); 
        
        // Verifique se o nome da sala foi atualizado para o nome de s2
        Sala salaAtualizada = salaRepository.findById("S_DUP").orElseThrow();
        assertEquals("Sala Duplicada", salaAtualizada.getNome());
        assertEquals(5, salaAtualizada.getCapacidade());
    }
    
    @Test
    @DisplayName("CT07: Deve falhar ao salvar sala com nome vazio (BindingResult Integração)")
    void integracao_CT07_ValidacaoDeController() {
        // Arrange: Cria uma sala inválida (nome vazio)
        Sala salaInvalida = new Sala("S_VALID", "", 10);
        // Cria um BindingResult real para esta sala
        BindingResult brInvalido = new BeanPropertyBindingResult(salaInvalida, "sala");
        // Simula o erro que o validador @NotBlank colocaria
        brInvalido.rejectValue("nome", "NotBlank", "O Nome é obrigatório");

        // Act
        String viewName = salaController.salvarSala(salaInvalida, brInvalido, redirectAttrs, model);

        // Assert
        // Deve retornar ao formulário, e não redirecionar
        assertEquals("sala-form", viewName);
        // Nenhum atributo de sucesso/erro de *redirecionamento* deve ser setado
        assertTrue(redirectAttrs.getFlashAttributes().isEmpty());
        // O banco não deve ter sido tocado
        assertEquals(0, salaRepository.count());
    }

    @Test
    @DisplayName("CT08: Deve remover associação de equipamento ao editar sala (Integração)")
    void integracao_CT08_RemoverAssociacao() {
        // Arrange
        Equipamento e1 = equipamentoRepository.save(new Equipamento("Projetor", ""));
        Sala s1 = new Sala("S_EDIT", "Sala com Equipamento", 10);
        s1.setEquipamentos(List.of(e1));

        // Act 1: Salvar a sala com o equipamento
        salaController.salvarSala(s1, bindingResult, redirectAttrs, model);
        
        // Assert 1: Verifica se foi salvo corretamente
        Sala salaSalva = salaRepository.findById("S_EDIT").orElseThrow();
        assertEquals(1, salaSalva.getEquipamentos().size());

        // Act 2: Salvar a mesma sala, mas com a lista de equipamentos vazia
        salaSalva.setEquipamentos(Collections.emptyList());
        BindingResult bindingResultEdit = new BeanPropertyBindingResult(salaSalva, "sala"); // Novo BindingResult
        redirectAttrs = new RedirectAttributesModelMap(); // Limpar atributos
        salaController.salvarSala(salaSalva, bindingResultEdit, redirectAttrs, model);

        // Assert 2: Verifica se a associação foi removida
        Sala salaAtualizada = salaRepository.findById("S_EDIT").orElseThrow();
        assertEquals(0, salaAtualizada.getEquipamentos().size());
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("CT09: Deve exibir salas e equipamentos na página principal (Integração)")
    void integracao_CT09_VisualizacaoDadosNaHome() {
        // Arrange: Configura o banco com dados
        Equipamento e1 = equipamentoRepository.save(new Equipamento("Projetor", ""));
        Sala s1 = new Sala("S_HOME", "Sala Home", 10);
        s1.setEquipamentos(List.of(e1));
        salaRepository.save(s1);

        // Act: Chama o método do controller da página principal
        Model modelHome = new BindingAwareModelMap();
        String viewName = reservaController.exibirFormulario(modelHome);

        // Assert
        assertEquals("index", viewName);
        
        // Verifica se os dados corretos estão no model
        @SuppressWarnings("unchecked")
        List<Sala> salasDoModel = (List<Sala>) modelHome.getAttribute("salas");
        
        assertNotNull(salasDoModel);
        assertEquals(1, salasDoModel.size());
        assertEquals("S_HOME", salasDoModel.get(0).getId());
        assertEquals(1, salasDoModel.get(0).getEquipamentos().size());
        assertEquals("Projetor", salasDoModel.get(0).getEquipamentos().get(0).getNome());
    }

    @Test
    @DisplayName("CT10: Deve apagar sala com sucesso após remover reservas (Integração)")
    void integracao_CT10_ExclusaoLimpaAposRemoverDependencia() {
        // Arrange
        Sala s1 = salaRepository.save(new Sala("S_DEL", "Sala Delete", 10));
        Reserva r1 = reservaRepository.save(new Reserva("Ana", s1, 
                            LocalDateTime.now().plusDays(1), 
                            LocalDateTime.now().plusDays(2)));

        // Act 1: Tentar apagar (deve falhar)
        salaController.apagarSala(s1.getId(), redirectAttrs);
        
        // Assert 1
        assertNotNull(redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertEquals(1, salaRepository.count()); // Sala ainda existe

        // Act 2: Remover a dependência (reserva)
        reservaRepository.delete(r1);
        redirectAttrs = new RedirectAttributesModelMap(); // Limpar atributos

        // Act 3: Tentar apagar novamente (deve funcionar)
        salaController.apagarSala(s1.getId(), redirectAttrs);

        // Assert 3
        assertNotNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertEquals(0, salaRepository.count()); // Sala foi apagada
    }
}