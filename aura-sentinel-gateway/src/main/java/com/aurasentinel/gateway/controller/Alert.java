package com.aurasentinel.gateway.controller;

import java.time.Instant;

// DTO para enviar dados de alerta, já processados e enriquecidos pela IA, para o Frontend Angular.
public class Alert {

    // Unique identifier of the Alert.
    private String alertId;

    // ID of the machine that triggered the alert.
    private String endpointId;

    // Involved user.
    private String userId;

    // Timestamp of alert creation.
    private Instant creationTimestamp;

    // Anomaly Score from the Machine Learning Model (0.0 to 1.0).
    private double mlScore;

    // The Cognitive Analysis from Gemini/LangChain (the natural language summary).
    private String cognitiveAnalysis;

    // The final AURA Confidence Score, after Gemini validation (0.0 to 1.0).
    private double auraConfidenceScore;

    // Current status of the alert: PENDENTE, MITIGADO, RISCO_REAL.
    private String status;

    // Empty constructor
    public Alert() {}
    
    // CONSTRUTOR COMPLETO ADICIONADO PARA SUPORTE À CRIAÇÃO DE DADOS MOCK
    public Alert(String alertId, String endpointId, String userId, Instant creationTimestamp, double mlScore, String cognitiveAnalysis, double auraConfidenceScore, String status) {
        this.alertId = alertId;
        this.endpointId = endpointId;
        this.userId = userId;
        this.creationTimestamp = creationTimestamp;
        this.mlScore = mlScore;
        this.cognitiveAnalysis = cognitiveAnalysis;
        this.auraConfidenceScore = auraConfidenceScore;
        this.status = status;
    }

    // --- Getters e Setters (Omitidos para brevidade) ---

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public String getEndpointId() { return endpointId; }
    public void setEndpointId(String endpointId) { this.endpointId = endpointId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Instant creationTimestamp) { this.creationTimestamp = creationTimestamp; }
    public double getMlScore() { return mlScore; }
    public void setMlScore(double mlScore) { this.mlScore = mlScore; }
    public String getCognitiveAnalysis() { return cognitiveAnalysis; }
    public void setCognitiveAnalysis(String cognitiveAnalysis) { this.cognitiveAnalysis = cognitiveAnalysis; }
    public double getAuraConfidenceScore() { return auraConfidenceScore; }
    public void setAuraConfidenceScore(double auraConfidenceScore) { this.auraConfidenceScore = auraConfidenceScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}