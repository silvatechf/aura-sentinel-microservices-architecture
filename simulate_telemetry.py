import requests
import json
import time
import uuid
import random
import base64

# --- Java Gateway Configuration (Must match application.yml) ---
GATEWAY_URL = "http://localhost:8080/api/v1/agent/telemetry"

# Credentials for the AGENT role (Must match SecurityConfig.java)
AUTH_USERNAME = "agent"  
AUTH_PASSWORD = "agente123" 

# --- SIMULATION FUNCTIONS ---

def generate_telemetry_event(event_type: str, endpoint_id: str, user_id: str, context: dict) -> dict:
    """Generates a dictionary mimicking the TelemetryEvent DTO."""
    return {
        "eventId": str(uuid.uuid4()),
        "endpointId": endpoint_id,
        "userId": user_id,
        "eventType": event_type,
        "timestamp": int(time.time()),
        "contextData": context
    }

def send_event(event: dict):
    """Sends the event to the Java Gateway using explicit Basic Auth header."""
    
    # Generates the Basic Auth header (Base64 Encode)
    credentials = f"{AUTH_USERNAME}:{AUTH_PASSWORD}"
    encoded_credentials = base64.b64encode(credentials.encode('utf-8')).decode('utf-8')
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Basic {encoded_credentials}"
    }
    
    # 1. Sends the HTTP POST request
    try:
        response = requests.post(
            GATEWAY_URL,
            json=event,
            headers=headers, 
            timeout=10
        )
        
        # 2. Checks the response status
        if response.status_code == 202:
            print(f"✅ Event {event['eventId']} sent successfully. Status: {response.status_code} (Accepted)")
        elif response.status_code == 401:
            print(f"❌ Authentication Failed (401). Check agent credentials in Java SecurityConfig.")
        elif response.status_code == 403:
            print(f"❌ Authorization Failed (403). User '{AUTH_USERNAME}' lacks the required ROLE.")
        else:
            print(f"⚠️ Error sending event. Status: {response.statusCode}. Response: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print(f"❌ ERROR: Could not connect to Java Gateway at {GATEWAY_URL}. Ensure Spring Boot is running on port 8080.")
    except Exception as e:
        print(f"❌ An unexpected error occurred: {e}")

# --- SIMULATED ATTACK SCENARIOS ---

def simulate_decoy_access_attack():
    """Simulates a CRITICAL attack: access to a Decoy file (ML score = 0.99)."""
    print("\n--- Simulation 1: DECOY_ACCESS (Critical Risk) ---")
    event = generate_telemetry_event(
        event_type="DECOY_ACCESS",
        endpoint_id="HR-LAPTOP-14",
        user_id="sara.smith",
        context={
            "filePath": "C:\\Windows\\system32\\decoy\\passwords.xlsx",
            "process": "explorer.exe",
            "action": "READ"
        }
    )
    send_event(event)

def simulate_file_write_anomaly():
    """Simulates a massive file write anomaly (ML score = 0.85)."""
    print("\n--- Simulation 2: FILE_WRITE Anomaly (High Risk) ---")
    event = generate_telemetry_event(
        event_type="FILE_WRITE",
        endpoint_id="SRV-FILES-05",
        user_id="system_backup_svc",
        context={
            "filePath": "C:\\Users\\Public\\Share\\mass_rename_batch_1.zip",
            "operationCount": 540,
            "durationMs": 5000 
        }
    )
    send_event(event)

def simulate_normal_behavior():
    """Simulates a low-risk authentication event (ML score = 0.05)."""
    print("\n--- Simulation 3: AUTH_FAIL Normal (Low Risk) ---")
    event = generate_telemetry_event(
        event_type="AUTH_FAIL",
        endpoint_id="PC-DEV-03",
        user_id="john.doe",
        context={
            "sourceIp": "192.168.1.5",
            "reason": "WrongPassword",
            "attempts": 2
        }
    )
    send_event(event)


if __name__ == "__main__":
    
    print("--- AURA Sentinel: Endpoint Telemetry Simulator ---")
    
    # Simulates the different attack scenarios
    simulate_decoy_access_attack()
    time.sleep(1)
    simulate_file_write_anomaly()
    time.sleep(1)
    simulate_normal_behavior()
    
    print("\nSimulation complete.")
