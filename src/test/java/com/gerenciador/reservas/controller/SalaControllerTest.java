package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.EquipamentoRepository;
import com.gerenciador.reservas.repository.SalaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaControllerTest {

    @InjectMocks
    private SalaController controller;

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @Test
    @DisplayName("RT01/CT01: Deve criar uma nova sala com sucesso")
    void salvarSala_CT01_CriarSala() {
        // Cenário (Arrange)
        Sala salaParaSalvar = new Sala("S10", "Sala de Teste", 10);
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarSala(salaParaSalvar, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    @DisplayName("RT01/CT02: Deve falhar ao salvar sala com ID duplicado")
    void salvarSala_CT02_IdDuplicado() {
        // Cenário (Arrange)
        Sala salaDuplicada = new Sala("S01", "Sala Duplicada", 5);
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Simula o repositório a lançar a exceção
        when(salaRepository.save(any(Sala.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de chave primária"));

        // Ação (Act)
        String viewName = controller.salvarSala(salaDuplicada, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);
        assertEquals("Erro: O ID da sala 'S01' já existe.", redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("RT01/CT03: Deve falhar ao salvar sala com campos vazios")
    void salvarSala_CT03_CamposVazios() {
        // Cenário (Arrange)
        Sala salaVazia = new Sala("", "", 0);
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Simula o BindingResult a dizer que "HÁ ERROS"
        when(bindingResult.hasErrors()).thenReturn(true);

        // Ação (Act)
        String viewName = controller.salvarSala(salaVazia, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("sala-form", viewName); // Retorna ao formulário
        verify(salaRepository, never()).save(any(Sala.class)); // Nunca salva
    }

    @Test
    @DisplayName("RT02/CT04: Deve editar uma sala com sucesso")
    void salvarSala_CT04_EditaSala() {
        // Cenário (Arrange)
        Sala salaEditada = new Sala("S01", "Sala Nova Editada", 20);
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarSala(salaEditada, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);
        assertEquals("Sala salva com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));

        ArgumentCaptor<Sala> captor = ArgumentCaptor.forClass(Sala.class);
        verify(salaRepository).save(captor.capture());

        Sala salaSalva = captor.getValue();
        assertEquals("S01", salaSalva.getId());
        assertEquals("Sala Nova Editada", salaSalva.getNome());
    }

    @Test
    @DisplayName("RT03/CT05: Deve apagar uma sala com sucesso")
    void apagarSala_CT05_ApagarSala() {
        // Cenário (Arrange)
        String idParaApagar = "S10";
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.apagarSala(idParaApagar, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);
        assertEquals("Sala apagada com sucesso!", redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
        verify(salaRepository).deleteById("S10");
    }

    @Test
    @DisplayName("RT01/CT06: Deve falhar ao tentar apagar sala inexistente")
    void apagarSala_CT06_SalaInexistente() {
        // Cenário (Arrange)
        String idInexistente = "S99";
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Simula o repositório a lançar a exceção
        doThrow(new EmptyResultDataAccessException(1))
                .when(salaRepository)
                .deleteById(idInexistente);

        // Ação (Act)
        String viewName = controller.apagarSala(idInexistente, redirectAttrs);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);
        assertEquals("Erro: Sala com ID S99 não foi encontrada.",
                redirectAttrs.getFlashAttributes().get("mensagemErro"));
        assertNull(redirectAttrs.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("RT07/CT12: Deve associar equipamentos a uma sala ao salvar")
    void salvarSala_CT12_AssociarEquipamentos() {
        // Cenário (Arrange)
        Equipamento eq1 = new Equipamento();
        eq1.setId(1L);
        Equipamento eq2 = new Equipamento();
        eq2.setId(2L);

        Sala salaParaSalvar = new Sala("S01", "Sala Editada", 10);
        salaParaSalvar.setEquipamentos(List.of(eq1, eq2));

        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarSala(salaParaSalvar, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);

        ArgumentCaptor<Sala> salaCaptor = ArgumentCaptor.forClass(Sala.class);
        verify(salaRepository).save(salaCaptor.capture());

        Sala salaSalva = salaCaptor.getValue();
        assertEquals(2, salaSalva.getEquipamentos().size());
    }

    @Test
    @DisplayName("RT08/CT13: Deve remover associação de equipamentos")
    void salvarSala_CT13_RemoveAssociacao() {
        // Cenário (Arrange)
        Sala salaSemEquip = new Sala("S01", "Sala Editada", 10);
        salaSemEquip.setEquipamentos(Collections.emptyList()); // Lista vazia
        RedirectAttributes redirectAttrs = new RedirectAttributesModelMap();

        // Ação (Act)
        String viewName = controller.salvarSala(salaSemEquip, bindingResult, redirectAttrs, model);

        // Verificação (Assert)
        assertEquals("redirect:/salas", viewName);

        ArgumentCaptor<Sala> captor = ArgumentCaptor.forClass(Sala.class);
        verify(salaRepository).save(captor.capture());

        Sala salaSalva = captor.getValue();
        assertEquals(0, salaSalva.getEquipamentos().size());
    }

    @Test
    @DisplayName("Deve exibir o formulário de edição com os dados corretos")
    void exibirFormularioEditarSala_CarregaDadosCorretos() {
        // Cenário (Arrange)
        Sala salaDoBanco = new Sala("S01", "Sala Antiga", 10);
        Equipamento eq1 = new Equipamento("Projetor", "");

        when(salaRepository.findById("S01")).thenReturn(Optional.of(salaDoBanco));
        when(equipamentoRepository.findAll()).thenReturn(List.of(eq1));

        Model modelParaTeste = new BindingAwareModelMap();

        // Ação (Act)
        String viewName = controller.exibirFormularioEditarSala("S01", modelParaTeste);

        // Verificação (Assert)
        assertEquals("sala-form", viewName);
        assertEquals(salaDoBanco, modelParaTeste.getAttribute("sala"));
        assertEquals(List.of(eq1), modelParaTeste.getAttribute("todosEquipamentos"));
    }
}