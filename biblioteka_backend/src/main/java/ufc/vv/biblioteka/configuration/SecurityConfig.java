package ufc.vv.biblioteka.configuration;

import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import ufc.vv.biblioteka.model.GerenciadorEmprestimoReserva;
import ufc.vv.biblioteka.model.GerenciadorRenovacao;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ISBNValidator isbnValidator() {
        return new ISBNValidator();
    }

    @Bean
    public GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva() {
        return new GerenciadorEmprestimoReserva();
    }

    @Bean
    public GerenciadorRenovacao gerenciadorRenovacao() {
        return new GerenciadorRenovacao();
    }
}