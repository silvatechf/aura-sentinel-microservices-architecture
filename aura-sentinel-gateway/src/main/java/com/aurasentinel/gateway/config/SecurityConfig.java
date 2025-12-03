package com.aurasentinel.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Enables web security
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- Injeção de Credenciais do application.yml ---
    
    // Injetamos as variáveis do bloco 'spring.security' no application.yml (usuário M2M)
    @Value("${spring.security.user.name}")
    private String internalUsername; 

    @Value("${spring.security.user.password}")
    private String internalPassword; 
    
    // Define a cadeia de filtros de segurança (SecurityFilterChain)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll() 
                
                // Endpoint do Agente (Telemetry) requer a ROLE AGENT
                .requestMatchers("/api/v1/agent/telemetry").hasRole("AGENT") 
                
                // Endpoint de Ingestão Interna (Python) requer a ROLE INTERNAL
                .requestMatchers("/api/v1/internal/alert-ingestion").hasRole("INTERNAL")
                
                // Endpoints do Dashboard (Angular) requerem autenticação (USER role)
                .requestMatchers("/api/v1/dashboard/**").authenticated() 
                
                .anyRequest().authenticated()
            )
            // Habilita a autenticação HTTP Basic
            .httpBasic(httpBasic -> httpBasic.init(http));
            
        return http.build();
    }
    
    // Define os usuários e seus Roles em memória (InMemory Users)
    // O prefixo {noop} é usado para senhas não criptografadas.
    @Bean
    public UserDetailsService userDetailsService() {
        
        // 1. Agent Credentials (Role: AGENT) - Usuário do nosso simulador/agente de endpoint
        // A senha é codificada diretamente para evitar erros de parsing do YAML.
        UserDetails agent = User.builder()
            .username("agent")
            .password("{noop}agente123")
            .roles("AGENT")
            .build();

        // 2. Internal Credentials (Role: INTERNAL) - Usuário do Backend Python (M2M)
        // Lendo do bloco spring.security.user do application.yml
        UserDetails internalUser = User.builder()
            .username(internalUsername) 
            // A senha já contém o prefixo {noop} no application.yml
            .password(internalPassword) 
            .roles("INTERNAL")
            .build();
            
        // 3. Dashboard User (Role: USER) - Usuário para o Frontend Angular
        UserDetails dashboardUser = User.builder()
            .username("dashboard-user")
            .password("{noop}dashboard123")
            .roles("USER") 
            .build();

        return new InMemoryUserDetailsManager(agent, internalUser, dashboardUser);
    }
}