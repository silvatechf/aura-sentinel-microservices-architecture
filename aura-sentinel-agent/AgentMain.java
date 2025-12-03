package com.aurasentinel.agent;

import com.aurasentinel.agent.client.ApiClient;
import com.aurasentinel.agent.model.TelemetryEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Classe principal do Agente de Endpoint AURA Sentinel.
 * Responsável por iniciar o cliente API e os serviços de monitoramento.
 */
public class AgentMain {

    private static final String ENDPOINT_ID = "HR-LAPTOP-14"; // ID estático para simulação
    private static final String USER_ID = System.getProperty("user.name");

    public static void main(String[] args) {
        System.out.println("AURA Sentinel Endpoint Agent starting...");

        // O Agente deve usar 'com.fasterxml.jackson.core:jackson-databind' para JSON
        // Certifique-se que esta dependência está no seu projeto Maven/Gradle para este módulo.

        // 1. Inicializa o cliente API (para enviar dados ao Gateway)
        ApiClient apiClient = new ApiClient();
        System.out.println("API Client initialized. Targeting: http://localhost:8080/api/v1");

        // 2. Inicia o monitoramento em um thread agendado
        // Em um sistema real, o monitoramento de arquivos e processos seria threads separados.
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Agendar uma tarefa para enviar um evento de teste CRÍTICO a cada 10 segundos (Simulação)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("--- Scheduling Telemetry Send ---");
                
                // Simulação: Cria um evento de acesso CRÍTICO a um arquivo Decoy
                Map<String, Object> context = new HashMap<>();
                context.put("filePath", "C:\\Sensitive\\DecoyFile.dat");
                context.put("processName", "malware_script.exe");
                
                TelemetryEvent criticalEvent = new TelemetryEvent(
                    ENDPOINT_ID, 
                    USER_ID, 
                    "DECOY_ACCESS", // O evento crítico que dispara o score 0.99
                    context
                );
                
                apiClient.sendTelemetry(criticalEvent);

            } catch (Exception e) {
                System.err.println("Error during scheduled telemetry send: " + e.getMessage());
            }
        }, 5, 10, TimeUnit.SECONDS); // Começa em 5s, repete a cada 10s

        System.out.println("Agent fully initialized. Monitoring events...");
        
        // Adiciona um gancho de desligamento para fechar o scheduler (apenas em teste/simulação)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Agent scheduler...");
            scheduler.shutdown();
        }));
    }
}