package com.aurasentinel.gateway.controller;

import com.aurasentinel.gateway.model.Alert;
import com.aurasentinel.gateway.service.AlertPersistenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for internal, secured communication.
 * This is primarily used by the Python Intelligence Backend (LangChain/Gemini)
 * to send fully processed and enriched Alerts back to the Gateway for storage.
 */
@RestController
@RequestMapping("/api/v1/internal")
public class InternalController {

    private final AlertPersistenceService alertPersistenceService;

    // Dependency injection of the persistence service
    public InternalController(AlertPersistenceService alertPersistenceService) {
        this.alertPersistenceService = alertPersistenceService;
    }

    /**
     * Endpoint to receive a fully processed Alert from the Python Intelligence Backend.
     * * @param alert The Alert object enriched by the AI (including cognitive analysis).
     * @return Response 201 (Created) upon successful reception.
     */
    @PostMapping("/alert-ingestion")
    public ResponseEntity<Void> ingestProcessedAlert(@Valid @RequestBody Alert alert) {
        
        // Delegates to the persistence service to save the final alert data (Firestore)
        alertPersistenceService.saveProcessedAlert(alert);

        // Logs the success (English, standard for security logs)
        System.out.println("ALERT RECEIVED from Python: " + alert.getAlertId() + " with AURA Score: " + alert.getAuraConfidenceScore());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}