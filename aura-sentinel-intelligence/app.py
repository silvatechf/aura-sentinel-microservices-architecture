import logging
from flask import Flask, request, jsonify
from typing import Dict, Any, List
import requests
import json
import time

# Biblioteca Python para chamadas HTTP
from requests.exceptions import RequestException

from config import Config
from models.anomaly_detector import AnomalyDetector 

# --- Setup ---
logging.basicConfig(level=logging.INFO, 
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Initialize Flask App
app = Flask(__name__)

# Initialize services
anomaly_detector = AnomalyDetector()

# API REST Endpoint
GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"


# --- Cognitive Analysis Function (CHAMADA REST DIRETA) ---
def run_cognitive_analysis(event_data: Dict[str, Any], ml_score: float, historical_events: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    Função de análise usando chamada HTTP POST direta (elimina problemas de SDK).
    """
    
    if not Config.GEMINI_API_KEY: 
        return {
            "cognitive_analysis": f"ERROR: API Key is missing. Defaulting to ML score.",
            "aura_confidence_score": ml_score,
            "decision": "FP"
        }

    # 1. System Instruction (para o payload JSON)
    system_instruction_text = (
        "You are a highly specialized L3 Security Analyst (AURA Cognitive Analyst). "
        "Your goal is to validate if an automated anomaly alert is a genuine attack (REAL) or a False Positive (FP) based on the full context. "
        "Provide a concise, single-paragraph analysis (max 50 words) and output a strict JSON format."
    )
    
    user_prompt = f"""
    CONTEXTUAL DATA:
    Endpoint ID: {event_data.get("endpointId")}
    User ID: {event_data.get("userId")}
    Event Type: {event_data.get("eventType")}
    ML Anomaly Score: {ml_score:.2f}
    """
    
    # 2. Constrói o Payload JSON para a API REST
    payload = {
        "contents": [{"parts": [{"text": user_prompt}]}],
        "config": {
            "systemInstruction": system_instruction_text,
            "responseMimeType": "application/json"
        }
    }
    
    try:
        # 3. Faz a chamada POST REST direta
        # NOTA: O método REST usa o parâmetro 'key' na URL para autenticação
        headers = {'Content-Type': 'application/json'}
        response = requests.post(
            f"{GEMINI_API_URL}?key={Config.GEMINI_API_KEY}",
            headers=headers,
            json=payload,
            timeout=10
        )
        response.raise_for_status() # Levanta exceção para 4xx/5xx

        # 4. Processa a resposta JSON (Robusto contra texto extra do Gemini)
        result = response.json()
        
        # Extrai o texto da resposta do Gemini
        json_text = result['candidates'][0]['content']['parts'][0]['text']
        
        # Tentar parsing com JSON
        json_response = json.loads(json_text) 
        
        return {
            "cognitive_analysis": json_response.get("analysis_summary", "No summary provided by Gemini."),
            "aura_confidence_score": json_response.get("confidence_score", 0.0),
            "decision": json_response.get("decision", "FP")
        }
    
    except (RequestException, KeyError, json.JSONDecodeError, IndexError) as e: 
        logger.error(f"Validation processing error (REST API): {e}")
        return {
            "cognitive_analysis": f"ERROR: Gemini REST API failed to generate insight: {e}. Defaulting to ML Score.",
            "aura_confidence_score": ml_score,
            "decision": "FP"
        }


# --- REST Endpoint for Ingesting Scoring Events (Restante do Código) ---

@app.route('/intelligence/api/v1/scoring', methods=['POST'])
def ingest_scoring_event():
    
    if not request.json:
        return jsonify({"error": "Invalid input format. Expected JSON."}), 400

    event_data = request.json
    event_id = event_data.get("eventId", "UNKNOWN_ID")
    endpoint_id = event_data.get("endpointId", "UNKNOWN_ENDPOINT")
    logger.info(f"Received scoring request for Event ID: {event_id} from {endpoint_id}")

    # 1. ML Anomaly Score Calculation
    ml_score = anomaly_detector.predict_anomaly_score(event_data)
    
    final_alert_data = {
        "alertId": f"ALERT-{event_id}",
        "endpointId": endpoint_id,
        "userId": event_data.get("userId"),
        "creationTimestamp": int(time.time()),
        "mlScore": ml_score,
        "cognitiveAnalysis": "",
        "auraConfidenceScore": ml_score,
        "status": "PENDENTE"
    }

    # 2. Cognitive Validation (Direct Gemini Call)
    if ml_score >= Config.SCORING_THRESHOLD:
        logger.warning(f"ML Score ({ml_score:.2f}) exceeds threshold. Triggering Direct Gemini Analysis.")
        
        historical_events = [] 

        gemini_result = run_cognitive_analysis(event_data, ml_score, historical_events)
        
        final_alert_data["cognitiveAnalysis"] = gemini_result["cognitive_analysis"]
        final_alert_data["aura_confidence_score"] = gemini_result["aura_confidence_score"]
        
        if final_alert_data["aura_confidence_score"] >= Config.AURA_ENFORCE_THRESHOLD:
             logger.critical(f"\n======== AÇÃO CRÍTICA SOAR PENDENTE ========\n"
                             f"!!! Risco de Encriptação detectado: {final_alert_data['endpointId']} !!!\n"
                             f"AURA Score: {final_alert_data['aura_confidence_score']:.2f}\n"
                             f"============================================")


    # 3. Final Alert Delivery
    
    # --- send_alert_to_gateway function (Inlined here for simplicity, typically imported) ---
    alert_endpoint = f"{Config.GATEWAY_BASE_URL}/api/v1/internal/alert-ingestion"
    
    try:
        response = requests.post(
            alert_endpoint,
            json=final_alert_data,
            auth=(Config.GATEWAY_USER, Config.GATEWAY_PASSWORD),
            timeout=5
        )
        response.raise_for_status()
        logger.info(f"Alert successfully sent to Gateway for persistence: {final_alert_data.get('alertId')}")
    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to send alert to Spring Gateway at {alert_endpoint}: {e}")
    # --- End of send_alert_to_gateway ---
    

    return jsonify({"status": "Accepted for processing", "alert_id": final_alert_data["alertId"]}), 202

if __name__ == '__main__':
    # Flask will run on port 8081
    app.run(port=8081, debug=True)