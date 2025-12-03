package com.aurasentinel.gateway.service;

import com.aurasentinel.gateway.model.Alert;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for saving the fully processed Alert data to the persistent store (Firestore).
 * This service closes the data loop from the Python Intelligence module back to the database.
 */
@Service
public class AlertPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(AlertPersistenceService.class);

    // In a production environment, this would hold the actual Firestore instance and configuration.

    /**
     * Saves the processed Alert to the database.
     * @param alert The Alert DTO containing the Gemini analysis and AURA Score.
     */
    public void saveProcessedAlert(Alert alert) {
        // --- Placeholder for Firestore Integration ---
        
        // This is where Firebase SDK for Java would be used to connect to Firestore
        // and save the data in the artifacts/{appId}/public/data/alerts collection.
        
        try {
            // Log the success (English, standard for security logs)
            log.info("Alert {} persisted successfully. AURA Score: {}.", 
                     alert.getAlertId(), 
                     alert.getAuraConfidenceScore());

        } catch (Exception e) {
            log.error("CRITICAL: Failed to persist Alert {} to the database: {}", 
                      alert.getAlertId(), e.getMessage());
            // This error must be monitored and handled as a system critical failure.
        }
    }
}