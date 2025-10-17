package com.gerenciador.reservas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Autoriza requisições HTTP
                .authorizeHttpRequests(auth -> auth
                        // Permite todas as requisições para o H2 console
                        .requestMatchers(toH2Console()).permitAll()
                        // Por enquanto, permite todas as outras requisições (ajustaremos isso depois)
                        .anyRequest().permitAll())
                // Desabilita CSRF (Cross-Site Request Forgery) especificamente para o H2
                // console
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(toH2Console()))
                // Permite que o H2 console seja exibido em um frame no navegador
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}