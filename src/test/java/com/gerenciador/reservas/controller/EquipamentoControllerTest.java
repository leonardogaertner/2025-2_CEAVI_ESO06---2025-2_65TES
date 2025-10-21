package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.repository.EquipamentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipamentoControllerTest {

    @InjectMocks
    private EquipamentoController controller;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private BindingResult bindingResult; // Mock para o BindingResult

    @Test
    @DisplayName("RT04/CT07: Deve criar um novo equipamento com sucesso")
    void salvarEquipamento_CT07_CriaEquipamento() {
        // Cenário (Arrange)
        Equipamento equipamento = new Equipamento("Projetor 4K", "Projetor de alta definição");
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarEquipamento(equipamento, bindingResult, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/equipamentos", viewName);
        assertEquals("Equipamento salvo com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));

        ArgumentCaptor<Equipamento> captor = ArgumentCaptor.forClass(Equipamento.class);
        verify(equipamentoRepository).save(captor.capture());
        assertEquals("Projetor 4K", captor.getValue().getNome());
    }

    @Test
    @DisplayName("RT04/CT08: Deve falhar ao salvar equipamento com nome vazio")
    void salvarEquipamento_CT08_CamposVazios() {
        // Cenário (Arrange)
        Equipamento equipVazio = new Equipamento("", "Teste");
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Simula o BindingResult a dizer que "HÁ ERROS"
        when(bindingResult.hasErrors()).thenReturn(true);

        // Ação (Act)
        String viewName = controller.salvarEquipamento(equipVazio, bindingResult, redirectAttrs);

        // Verificação (Assert)
        assertEquals("equipamento-form", viewName);
        verify(equipamentoRepository, never()).save(any(Equipamento.class));
    }

    @Test
    @DisplayName("RT05/CT09: Deve editar um equipamento com sucesso")
    void salvarEquipamento_CT09_EditaEquipamento() {
        // Cenário (Arrange)
        Equipamento equipamento = new Equipamento("Projetor Novo Modelo", "Descrição mantida");
        equipamento.setId(1L); // Indica que é uma edição
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarEquipamento(equipamento, bindingResult, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/equipamentos", viewName);
        assertEquals("Equipamento salvo com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));

        ArgumentCaptor<Equipamento> captor = ArgumentCaptor.forClass(Equipamento.class);
        verify(equipamentoRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
    }

    @Test
    @DisplayName("RT06/CT10: Deve apagar um equipamento com sucesso")
    void apagarEquipamento_CT10_ApagaEquipamento() {
        // Cenário (Arrange)
        Long idParaApagar = 1L;
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.apagarEquipamento(idParaApagar, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/equipamentos", viewName);
        assertEquals("Equipamento apagado com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        verify(equipamentoRepository).deleteById(idParaApagar);
    }

    @Test
    @DisplayName("RT06/CT11: Deve falhar ao tentar apagar equipamento inexistente")
    void apagarEquipamento_CT11_EquipamentoInexistente() {
        // Cenário (Arrange)
        Long idInexistente = 999L;
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Simula o repositório a lançar a exceção
        doThrow(new EmptyResultDataAccessException(1))
                .when(equipamentoRepository)
                .deleteById(idInexistente);

        // Ação (Act)
        String viewName = controller.apagarEquipamento(idInexistente, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/equipamentos", viewName);
        assertEquals("Erro: Equipamento com ID 999 não foi encontrado.",
                redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
    }
}