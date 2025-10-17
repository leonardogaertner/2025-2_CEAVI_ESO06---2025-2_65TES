package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Equipamento;
import com.gerenciador.reservas.repository.EquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String salvarEquipamento(@ModelAttribute Equipamento equipamento, RedirectAttributes redirectAttributes) {
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
        equipamentoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Equipamento apagado com sucesso!");
        return "redirect:/equipamentos";
    }
}