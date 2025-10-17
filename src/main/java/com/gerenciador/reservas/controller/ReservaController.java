package com.gerenciador.reservas.controller;

import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.service.GerenciadorDeReservas;
import com.gerenciador.reservas.viewModel.ReservaViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReservaController {

    @Autowired
    private GerenciadorDeReservas gerenciador;

    // O UsuarioRepository foi REMOVIDO

    @GetMapping("/")
    public String exibirFormulario(Model model) {
        List<Reserva> reservas = gerenciador.getReservas();
        List<Sala> salasDisponiveis = gerenciador.getSalas();

        List<ReservaViewModel> reservasViewModel = reservas.stream().map(reserva -> {
            String nomeSala = reserva.getSala().getNome();
            // LÓGICA ATUALIZADA
            String nomeResponsavel = reserva.getNomeResponsavel();

            return new ReservaViewModel(nomeResponsavel, nomeSala, reserva.getDataInicio(),
                    reserva.getDataFim());
        }).collect(Collectors.toList());

        model.addAttribute("reservas", reservasViewModel);
        model.addAttribute("salas", salasDisponiveis);

        return "index";
    }

    @PostMapping("/reservar")
    public String processarReserva(@RequestParam String idSala,
            @RequestParam String nomeResponsavel, // Apenas o nome é necessário agora
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Model model) {
        try {
            Sala sala = gerenciador.getSalaById(idSala);

            // LÓGICA SIMPLIFICADA
            gerenciador.reservarSala(sala, nomeResponsavel, dataInicio, dataFim);

            model.addAttribute("mensagemSucesso", "Reserva realizada com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }

        return exibirFormulario(model);
    }
}