package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.repository.EquipamentoRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.EmptyResultDataAccessException;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoController {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @GetMapping
    public String listarEquipamentos(Model model) {
        model.addAttribute("equipamentos", equipamentoRepository.findAll());
        return "equipamentos-lista";
    }

    @GetMapping("/novo")
    public String exibirFormularioNovo(Model model) {
        model.addAttribute("equipamento", new Equipamento());
        return "equipamento-form";
    }

    @PostMapping("/salvar")
    public String salvarEquipamento(@Valid @ModelAttribute Equipamento equipamento,
            BindingResult bindingResult, // 3. ADICIONAR
            RedirectAttributes redirectAttributes) {

        // 4. ADICIONAR LÓGICA DE VALIDAÇÃO
        if (bindingResult.hasErrors()) {
            // Retorna para o formulário para mostrar os erros
            return "equipamento-form";
        }

        equipamentoRepository.save(equipamento);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Equipamento salvo com sucesso!");
        return "redirect:/equipamentos";
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEditar(@PathVariable Long id, Model model) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Equipamento inválido Id:" + id));
        model.addAttribute("equipamento", equipamento);
        return "equipamento-form";
    }

    @GetMapping("/apagar/{id}")
    public String apagarEquipamento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // 2. ADICIONAR TRY-CATCH
        try {
            equipamentoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Equipamento apagado com sucesso!");
        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro: Equipamento com ID " + id + " não foi encontrado.");
        }
        return "redirect:/equipamentos";
    }
}