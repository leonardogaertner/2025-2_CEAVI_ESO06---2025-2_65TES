package com.gerenciador.reservas.controller;

import org.springframework.dao.EmptyResultDataAccessException;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.EquipamentoRepository; // Importar o repositório
import com.gerenciador.reservas.repository.SalaRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @GetMapping
    public String listarSalas(Model model) {
        List<Sala> salas = salaRepository.findAll();
        model.addAttribute("salas", salas);
        return "salas-lista";
    }

    @GetMapping("/nova")
    public String exibirFormularioNovaSala(Model model) {
        // Envia uma lista de todos os equipamentos para o formulário
        model.addAttribute("todosEquipamentos", equipamentoRepository.findAll());
        model.addAttribute("sala", new Sala());
        return "sala-form";
    }

    @PostMapping("/salvar")
    public String salvarSala(@Valid @ModelAttribute Sala sala,
            BindingResult bindingResult, // 4. ADICIONAR
            RedirectAttributes redirectAttributes,
            Model model) { // 5. ADICIONAR

        // 6. ADICIONAR LÓGICA DE VALIDAÇÃO
        if (bindingResult.hasErrors()) {
            // Se houver erros, não tenta salvar.
            // Retorna para o formulário para mostrar os erros.
            model.addAttribute("todosEquipamentos", equipamentoRepository.findAll()); // Recarrega os equipamentos
            return "sala-form";
        }

        try {
            salaRepository.save(sala);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sala salva com sucesso!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("mensagemErro",
                    "Erro: O ID da sala '" + sala.getId() + "' já existe.");
        }
        return "redirect:/salas";
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEditarSala(@PathVariable String id, Model model) {
        Optional<Sala> salaOptional = salaRepository.findById(id);
        if (!salaOptional.isPresent()) {
            return "redirect:/salas";
        }
        // Envia a lista de todos os equipamentos também para o formulário de edição
        model.addAttribute("todosEquipamentos", equipamentoRepository.findAll());
        model.addAttribute("sala", salaOptional.get());
        return "sala-form";
    }

    // ... (o método apagarSala permanece o mesmo) ...
    @GetMapping("/apagar/{id}")
    public String apagarSala(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            salaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Sala apagada com sucesso!");

        } catch (EmptyResultDataAccessException e) {
            // 2. CAPTURAR A EXCEÇÃO
            // Isto acontece se o ID não for encontrado
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro: Sala com ID " + id + " não foi encontrada.");
        }

        return "redirect:/salas";
    }
}