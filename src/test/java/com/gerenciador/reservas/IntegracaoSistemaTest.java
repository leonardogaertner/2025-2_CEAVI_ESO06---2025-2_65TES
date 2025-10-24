package com.gerenciador.reservas;

import com.gerenciador.reservas.controller.EquipamentoController;
import com.gerenciador.reservas.controller.ReservaController;
import com.gerenciador.reservas.controller.SalaController;
import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.EquipamentoRepository;
import com.gerenciador.reservas.repository.ReservaRepository;
import com.gerenciador.reservas.repository.SalaRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
}