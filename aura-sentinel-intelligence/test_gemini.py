import requests
import json
from config import GEMINI_API_KEY

GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateMessage"

def test_gemini_api():
    if not GEMINI_API_KEY:
        print("❌ GEMINI_API_KEY não está definida no config.py")
        return

    payload = {
        "prompt": {
            "messages": [
                {"author": "system", "content": "Você é um assistente de teste."},
                {"author": "user", "content": "Teste de conexão"}
            ]
        },
        "temperature": 0,
        "candidateCount": 1,
        "topK": 40
    }

    try:
        response = requests.post(
            f"{GEMINI_API_URL}?key={GEMINI_API_KEY}",
            headers={"Content-Type": "application/json"},
            json=payload,
            timeout=10
        )
        response.raise_for_status()
        data = response.json()
        print("✅ Conexão bem-sucedida com Gemini!")
        print(json.dumps(data, indent=2))

    except requests.exceptions.HTTPError as he:
        print(f"❌ Erro HTTP: {he} - Verifique se a chave API está correta e habilitada")
    except requests.exceptions.Timeout:
        print("❌ Timeout: Servidor não respondeu. Verifique a conexão de rede")
    except requests.exceptions.RequestException as e:
        print(f"❌ Erro de requisição: {e}")
    except json.JSONDecodeError:
        print("❌ Falha ao decodificar JSON da resposta")

if __name__ == "__main__":
    test_gemini_api()
