package com.aurasentinel.agent.model;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;

/**
 * Data Transfer Object (DTO) for Telemetry Events.
 * This structure must match the TelemetryEvent DTO in the Spring Gateway
 * to ensure successful JSON serialization and ingestion.
 */
public class TelemetryEvent {

    // Unique identifier for the event.
    public String eventId;

    // Unique identifier for the client machine (endpoint).
    public String endpointId;

    // ID of the user or system account involved.
    public String userId;

    // Type of event: FILE_WRITE, AUTH_FAIL, DECOY_ACCESS, etc.
    public String eventType;

    // Timestamp when the event occurred on the endpoint.
    public long timestamp; // Using long for Unix timestamp (seconds/milliseconds)

    // Specific context data (e.g., file path, command line arguments).
    public Map<String, Object> contextData;

    // Constructor required by the ObjectMapper for deserialization (optional but good practice)
    public TelemetryEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now().getEpochSecond(); // Default to current time in seconds
    }

    // Constructor used by the Agent to create events
    public TelemetryEvent(String endpointId, String userId, String eventType, Map<String, Object> contextData) {
        this(); // Call default constructor for ID and timestamp
        this.endpointId = endpointId;
        this.userId = userId;
        this.eventType = eventType;
        this.contextData = contextData;
    }
}