package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Equipamento; // Importar o modelo Equipamento
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.repository.EquipamentoRepository; // Importar o repositório
import com.gerenciador.reservas.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaRepository salaRepository;

    // NOVO: Injetar o repositório de equipamentos
    @Autowired
    private EquipamentoRepository equipamentoRepository;

    // ... (o método listarSalas permanece o mesmo) ...
    @GetMapping
    public String listarSalas(Model model) {
        List<Sala> salas = salaRepository.findAll();
        model.addAttribute("salas", salas);
        return "salas-lista";
    }

    // ATUALIZADO: Método para exibir o formulário de nova sala
    @GetMapping("/nova")
    public String exibirFormularioNovaSala(Model model) {
        // Envia uma lista de todos os equipamentos para o formulário
        model.addAttribute("todosEquipamentos", equipamentoRepository.findAll());
        model.addAttribute("sala", new Sala());
        return "sala-form";
    }

    // ... (o método salvarSala permanece o mesmo, o data binding do Spring cuidará
    // da lista) ...
    @PostMapping("/salvar")
    public String salvarSala(@ModelAttribute Sala sala, RedirectAttributes redirectAttributes) {
        salaRepository.save(sala);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sala salva com sucesso!");
        return "redirect:/salas";
    }

    // ATUALIZADO: Método para exibir o formulário de edição
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
        salaRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Sala apagada com sucesso!");
        return "redirect:/salas";
    }
}