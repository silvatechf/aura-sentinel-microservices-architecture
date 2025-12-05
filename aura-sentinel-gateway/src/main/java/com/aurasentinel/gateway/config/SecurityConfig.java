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
            .httpBasic(httpBasic -> httpBasic.init(http));
            
        return http.build();
    }
    
    
    @Bean
    public UserDetailsService userDetailsService() {
        
        
        UserDetails agent = User.builder()
            .username("agent")
            .password("{noop}agente123")
            .roles("AGENT")
            .build();

        // 2. Internal Credentials 
        UserDetails internalUser = User.builder()
            .username(internalUsername) 
            .password(internalPassword) 
            .roles("INTERNAL")
            .build();
            
        // 3. Dashboard User 
        UserDetails dashboardUser = User.builder()
            .username("dashboard-user")
            .password("{noop}dashboard123")
            .roles("USER") 
            .build();

        return new InMemoryUserDetailsManager(agent, internalUser, dashboardUser);
    }
}
