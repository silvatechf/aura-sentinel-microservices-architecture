package com.aurasentinel.gateway.controller;

import com.aurasentinel.gateway.model.TelemetryEvent;
import com.aurasentinel.gateway.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller responsible for receiving and processing telemetry events from Agents.
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final IngestionService ingestionService;

    // Dependency injection of the ingestion service
    public AgentController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Endpoint for ingesting a single telemetry event.
     * @param event The validated TelemetryEvent object.
     * @return Response 202 (Accepted). Ingestion must be ASYNCHRONOUS and FAST.
     */
    @PostMapping("/telemetry")
    public ResponseEntity<Void> ingestSingleTelemetryEvent(@Valid @RequestBody TelemetryEvent event) {
        
        ingestionService.processTelemetryEvent(event);

        // Return HTTP 202 Accepted.
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}