import requests
import json
import time
import uuid
import random
import base64

# --- Configurações do Gateway Java (Deve corresponder ao application.yml) ---
GATEWAY_URL = "http://localhost:8080/api/v1/agent/telemetry"
# Credenciais Literais (SEM O PREFIXO {noop})
# IMPORTANTE: Estas credenciais correspondem ao ROLE AGENT no SecurityConfig.java
AUTH_USERNAME = "agent"  
AUTH_PASSWORD = "agente123" 

# --- FUNÇÕES DE SIMULAÇÃO ---

def generate_telemetry_event(event_type: str, endpoint_id: str, user_id: str, context: dict) -> dict:
    """Gera um dicionário que imita o DTO TelemetryEvent.java."""
    return {
        "eventId": str(uuid.uuid4()),
        "endpointId": endpoint_id,
        "userId": user_id,
        "eventType": event_type,
        "timestamp": int(time.time()),
        "contextData": context
    }

def send_event(event: dict):
    """Envia o evento para o Gateway Java com cabeçalho de autenticação explícito."""
    
    # Gera o cabeçalho Basic Auth (Python -> Base64)
    credentials = f"{AUTH_USERNAME}:{AUTH_PASSWORD}"
    encoded_credentials = base64.b64encode(credentials.encode('utf-8')).decode('utf-8')
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Basic {encoded_credentials}"
    }
    
    # 1. Envia a requisição POST
    try:
        response = requests.post(
            GATEWAY_URL,
            json=event,
            headers=headers, # Usando o cabeçalho explícito
            timeout=10
        )
        
        # 2. Verifica o status da resposta
        if response.status_code == 202:
            print(f"✅ Evento {event['eventId']} enviado com sucesso. Status: {response.status_code} (Accepted)")
        elif response.status_code == 401:
            print(f"❌ Falha de Autenticação (401). Credenciais incorretas ou endpoint não existe.")
        elif response.status_code == 403:
            print(f"❌ Falha de Autorização (403). O usuário '{AUTH_USERNAME}' não tem a ROLE AGENT.")
        else:
            print(f"⚠️ Erro ao enviar evento. Status: {response.status_code}. Resposta: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print(f"❌ ERRO: Não foi possível conectar ao Gateway Java em {GATEWAY_URL}. Verifique se o serviço Spring Boot está rodando na porta 8080.")
    except Exception as e:
        print(f"❌ Ocorreu um erro: {e}")

# --- CENÁRIOS DE ATAQUE SIMULADOS ---

def simulate_decoy_access_attack():
    """Simula um ataque CRÍTICO: acesso a um arquivo Decoy (pontuação ML = 0.99)."""
    print("\n--- Simulação 1: DECOY_ACCESS (Risco Crítico) ---")
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
    """Simula uma anomalia de escrita massiva (pontuação ML = 0.85)."""
    print("\n--- Simulação 2: FILE_WRITE Anômala (Risco Alto) ---")
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
    """Simula um evento de baixo risco (pontuação ML = 0.05)."""
    print("\n--- Simulação 3: AUTH_FAIL Normal (Baixo Risco) ---")
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
    
    print("--- AURA Sentinel: Agent Telemetry Simulator ---")
    
    # Simula os diferentes cenários de ataque
    simulate_decoy_access_attack()
    time.sleep(1)
    simulate_file_write_anomaly()
    time.sleep(1)
    simulate_normal_behavior()
    
    print("\nSimulação concluída.")