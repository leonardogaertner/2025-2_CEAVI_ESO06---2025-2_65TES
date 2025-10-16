package com.gerenciador.reservas.controller;

import org.springframework.format.annotation.DateTimeFormat;
import com.gerenciador.reservas.model.Sala;
import com.gerenciador.reservas.service.GerenciadorDeReservas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.gerenciador.reservas.model.Reserva;
import com.gerenciador.reservas.model.ReservaViewModel;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ReservaController {

    // Por enquanto, vamos criar uma instância do nosso gerenciador aqui.
    // Mais tarde, com injeção de dependência, o Spring fará isso automaticamente.
    private final GerenciadorDeReservas gerenciador = new GerenciadorDeReservas();
    private final List<Sala> salasDisponiveis = new ArrayList<>();

    // Construtor para inicializar o sistema com alguns dados de exemplo.
    public ReservaController() {
        // Vamos adicionar algumas salas para que o formulário não fique vazio.
        salasDisponiveis.add(new Sala("S01", "Sala de Reunião 1", 10));
        salasDisponiveis.add(new Sala("S02", "Auditório", 50));
        salasDisponiveis.add(new Sala("S03", "Sala de Foco", 4));

        for (Sala sala : salasDisponiveis) {
            gerenciador.adicionarSala(sala);
        }
    }

    /**
     * Este método é chamado quando o usuário acessa a página principal.
     * Ele prepara os dados que a página precisa exibir.
     * 
     * @param model O objeto que levará os dados do Java para o HTML.
     * @return O nome do arquivo HTML que deve ser renderizado.
     */
    @GetMapping("/")
    public String exibirFormulario(Model model) {
        // 1. Pega as reservas do gerenciador
        List<Reserva> reservas = gerenciador.getReservas();

        // 2. Transforma a lista de Reserva em uma lista de ReservaViewModel
        List<ReservaViewModel> reservasViewModel = reservas.stream().map(reserva -> {
            // Para cada reserva, encontra a sala correspondente na lista de salas
            String nomeSala = salasDisponiveis.stream()
                    .filter(sala -> sala.getId().equals(reserva.getIdSala()))
                    .findFirst()
                    .map(Sala::getNome) // Pega o nome da sala
                    .orElse("Sala não encontrada"); // Um valor padrão caso não encontre

            // Cria o nosso objeto de view com os dados prontos
            return new ReservaViewModel(reserva.getNomeResponsavel(), nomeSala, reserva.getDataInicio(),
                    reserva.getDataFim());
        }).collect(Collectors.toList());

        // 3. Adiciona a nova lista (já processada) ao model
        model.addAttribute("reservas", reservasViewModel); // AGORA ESTAMOS ENVIANDO A LISTA NOVA
        model.addAttribute("salas", salasDisponiveis);
        model.addAttribute("novaReserva", new Object());

        return "index";
    }

    /**
     * Este método é chamado quando o usuário submete o formulário de reserva.
     * Ele recebe os dados do formulário como parâmetros.
     */
    @PostMapping("/reservar")
    public String processarReserva(@RequestParam String idSala,
            @RequestParam String nomeResponsavel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            Model model) {
        try {
            // Tenta executar a lógica de negócio que já criamos
            gerenciador.reservarSala(idSala, nomeResponsavel, dataInicio, dataFim);
            // Se der certo, adiciona uma mensagem de sucesso.
            model.addAttribute("mensagemSucesso", "Reserva realizada com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Se uma de nossas exceções for lançada, adiciona uma mensagem de erro.
            model.addAttribute("mensagemErro", e.getMessage());
        }

        // Após processar, chama o método de exibir o formulário novamente
        // para recarregar a página com os dados atualizados (nova lista de reservas e
        // mensagens).
        return exibirFormulario(model);
    }
}