package com.aurasentinel.agent.client;

import com.aurasentinel.agent.model.TelemetryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Cliente API responsável pela comunicação segura (M2M) do Agente de Endpoint
 * com o Gateway Central (Spring Boot).
 * * Utiliza HTTP Basic Authentication com as credenciais do ROLE AGENT.
 */
public class ApiClient {

    private static final String GATEWAY_BASE_URL = "http://localhost:8080/api/v1";
    private static final String TELEMETRY_ENDPOINT = "/agent/telemetry";
    
    // Credenciais fixas para a ROLE AGENT (Deve ser 'agent:agente123')
    private static final String AUTH_USER = "agent";
    private static final String AUTH_PASS = "agente123";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String authHeaderValue;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
        
        // Constrói o valor do cabeçalho HTTP Basic Auth (Base64 Encode)
        String authString = AUTH_USER + ":" + AUTH_PASS;
        this.authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Envia um evento de telemetria para o Gateway de forma síncrona.
     * @param event O evento de telemetria a ser enviado.
     * @return true se o evento foi aceito (HTTP 202), false caso contrário.
     */
    public boolean sendTelemetry(TelemetryEvent event) {
        String url = GATEWAY_BASE_URL + TELEMETRY_ENDPOINT;
        
        try {
            // 1. Serializa o objeto Java em JSON
            String requestBody = objectMapper.writeValueAsString(event);
            
            // 2. Constrói a requisição HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeaderValue) // Adiciona o cabeçalho Basic Auth
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(java.time.Duration.ofSeconds(4))
                    .build();

            // 3. Envia a requisição
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Verifica o status (Gateway retorna 202 Accepted)
            if (response.statusCode() == 202) {
                System.out.println("[API_CLIENT] Telemetry sent successfully (202 ACCEPTED): " + event.eventId);
                return true;
            } else {
                System.err.println("[API_CLIENT] Failed to send telemetry. Status: " + response.statusCode());
                System.err.println("[API_CLIENT] Response Body: " + response.body());
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[API_CLIENT] Connection/IO Error when sending telemetry: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[API_CLIENT] Unexpected error: " + e.getMessage());
            return false;
        }
    }
    
    // Futuras funções SOAR:
    // public void receiveSoarCommand() { ... }
}