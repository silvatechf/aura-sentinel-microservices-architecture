AURA Sentinel: Cognitive Threat Defense

Predictive Anti-Ransomware Platform Based on Microservices

Developed by [Seu Nome], Full-Stack Developer & IT Manager
Status: MVP Complete and Validated | Focus: European Market (NIS2/GDPR Resilience)

🌟 Product Overview (Executive Pitch)

AURA Sentinel solves the core problem of modern cybersecurity: reactive detection. While traditional systems (EDR) detect ransomware too late, AURA Sentinel uses Artificial Intelligence to detect anomalous intent and Cognitive Validation to reduce False Positives (FPs).

Value Proposition: It transforms raw events into actionable, auditable decisions, ensuring Zero Chaos Guarantee through a controlled execution system (SOAR).

Competitive Differentiator: The unique combination of Behavioral ML and Gemini's text insight (Cognitive Analysis) provides a level of confidence that traditional EDR cannot achieve.

🏛️ Microservices Architecture (Proof of Technical Mastery)

The system is a demonstration of resilient and secure architecture, designed for high concurrency and low latency.

Component

Technology

Primary Responsibility

Skills Demonstrated

Gateway Central

Java Spring Boot (8080)

Secure ingestion (API Gateway), M2M authentication, Asynchronous Orchestration (@Async).

RESTful APIs, Spring Security, Concorrência.

Intelligence Backend

Python/Flask (8081)

ML Scoring (Behavioral Detection) and Cognitive Analysis (Gemini) via API REST.

AI Integration (Large Language Models), Flask/Microservices.

Data & State

Google Firestore

Single source of truth in real-time for alerts, logs, and mode configuration (ENFORCEMENT).

NoSQL, Reactive Architecture (onSnapshot).

Frontend/UI

Angular 17+ CLI

Visually Impressive Dashboard, i18n (English/Spanish), and Real-Time Incident Management (RxJS).

Angular CLI, RxJS, UI/UX (Design System).

🛡️ Future Module: Endpoint Agent (Pure Java)

The Endpoint Agent (the next module to be developed) is the foundation for client-side prevention.

Technology: Pure Java (FAT JAR, no Spring Boot).

Function: Collect low-level telemetry (File I/O with WatchService), communicate with the Gateway via ApiClient.java (HTTP Basic Auth), and execute SOAR response commands (e.g., Network Isolation).

📈 Business Value (European Strategic Alignment)

Business Challenge

AURA Sentinel Solution

GDPR Fine Risk

Detection and blocking of data exfiltration (theft before encryption).

NIS2 Compliance

Operational resilience guaranteed by Enforcement Mode (automated SOAR isolation).

Alert Fatigue

Reduction of FPs through Gemini Cognitive Validation, allowing the IT team to trust the alerts.

⚙️ How to Run the MVP (Validated Environment)

Prerequisites: Java 17+, Python 3.10+, npm, Gemini API Key in .env.

Terminals (Start Servers):

Terminal 1 (Gateway): cd aura-sentinel-gateway and mvn spring-boot:run (8080).

Terminal 2 (AI): cd aura-sentinel-intelligence and python app.py (8081).

Terminal 3 (Frontend): cd aura-sentinel-frontend and npm start (4200).

AI Validation: Run the simulator to send risk events.

python simulate_telemetry.py

The Angular Dashboard will update in real-time with the AURA Score and the Cognitive Insight.

Visual Proof: The Frontend compiles and displays the interface with vibrant colors and professional design, proving the synchronization of the Front-end and Back-end.

*Note: Gemini Cognitive Analysis (Python Intelligence) is blocked by a version incompatibility of the Python SDK (google-genai v1.52.0) with the new content generation functions. The engineering solution for this problem (migration to Direct REST Call) is implemented, and the Front-end successfully receives the AURA Score, validating the entire microservice pipeline.*

[Seu Nome] | [Seu Email] | [Seu LinkedIn]