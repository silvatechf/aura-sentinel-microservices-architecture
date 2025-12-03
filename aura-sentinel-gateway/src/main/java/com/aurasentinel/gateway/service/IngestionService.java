package com.aurasentinel.gateway.service;

import com.aurasentinel.gateway.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// Service responsible for processing and forwarding telemetry events asynchronously.
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    
    private final PythonClientService pythonClientService;
    // FirestoreService firestoreService; // To be implemented later

    public IngestionService(PythonClientService pythonClientService) {
        this.pythonClientService = pythonClientService;
    }

    /**
     * Starts the processing pipeline for a telemetry event.
     * @Async ensures this runs on a separate thread, keeping the Controller fast.
     */
    @Async
    public void processTelemetryEvent(TelemetryEvent event) {
        
        // 1. Logging and Auditing (Security Standard: English)
        log.info("Ingesting event: {} from endpoint: {}", event.getEventId(), event.getEndpointId());
        
        try {
            // 2. RAW Data Persistence (Placeholder for Firestore)
            log.debug("Event {} persisted successfully (Placeholder).", event.getEventId());

            // 3. Forward to Intelligence Module (Python/LangChain)
            pythonClientService.sendToAIScoring(event);
            
            log.info("Event {} forwarded to AI Scoring.", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process or forward event {}: {}", event.getEventId(), e.getMessage());
            // In a production environment, this would trigger a system health alert.
        }
    }
}