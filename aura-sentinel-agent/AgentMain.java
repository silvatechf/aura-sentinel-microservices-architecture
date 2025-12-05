package com.aurasentinel.agent;

import com.aurasentinel.agent.client.ApiClient;
import com.aurasentinel.agent.model.TelemetryEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// main class
public class AgentMain {

    private static final String ENDPOINT_ID = "HR-LAPTOP-14"; // ID estático para simulação
    private static final String USER_ID = System.getProperty("user.name");

    public static void main(String[] args) {
        System.out.println("AURA Sentinel Endpoint Agent starting...");

        
        // 1. begin customer api
        ApiClient apiClient = new ApiClient();
        System.out.println("API Client initialized. Targeting: http://localhost:8080/api/v1");

        
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("--- Scheduling Telemetry Send ---");
                
                
                Map<String, Object> context = new HashMap<>();
                context.put("filePath", "C:\\Sensitive\\DecoyFile.dat");
                context.put("processName", "malware_script.exe");
                
                TelemetryEvent criticalEvent = new TelemetryEvent(
                    ENDPOINT_ID, 
                    USER_ID, 
                    "DECOY_ACCESS", 
                    context
                );
                
                apiClient.sendTelemetry(criticalEvent);

            } catch (Exception e) {
                System.err.println("Error during scheduled telemetry send: " + e.getMessage());
            }
        }, 5, 10, TimeUnit.SECONDS); // Começa em 5s, repete a cada 10s

        System.out.println("Agent fully initialized. Monitoring events...");
        
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Agent scheduler...");
            scheduler.shutdown();
        }));
    }
}
