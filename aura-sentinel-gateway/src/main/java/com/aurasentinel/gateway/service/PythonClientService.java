package com.aurasentinel.gateway.service;

import com.aurasentinel.gateway.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// Service responsible for communicating with the Intelligence Backend (Python/LangChain).
@Service
public class PythonClientService {

    private static final Logger log = LoggerFactory.getLogger(PythonClientService.class);
    private final WebClient webClient;
    
    private static final String SCORING_ENDPOINT = "/api/v1/scoring";

    // Constructor: WebClient is injected and built with configurations from application.yml (future step)
    public PythonClientService(WebClient.Builder webClientBuilder) {
        // Base URL is configured in application.yml
        this.webClient = webClientBuilder
            .baseUrl("http://localhost:8081/intelligence") // Placeholder, real value comes from config
            .build();
    }

    /**
     * Sends the telemetry event to the AI module for scoring.
     * This is a non-blocking notification; the ML returns the Alert through a separate API call later.
     */
    public void sendToAIScoring(TelemetryEvent event) {
        try {
            log.debug("Attempting to send event {} to Python ML...", event.getEventId());
            
            this.webClient.post()
                .uri(SCORING_ENDPOINT)
                .bodyValue(event)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                          clientResponse -> {
                              log.error("Python Scoring Service returned error status: {}", clientResponse.statusCode());
                              return clientResponse.createException();
                          })
                .bodyToMono(Void.class)
                .block(); 
            
            log.debug("Event {} successfully forwarded to Python.", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error communicating with Python Intelligence Backend for event {}: {}", event.getEventId(), e.getMessage());
        }
    }
}