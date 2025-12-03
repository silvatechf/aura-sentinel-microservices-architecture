import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { initializeApp } from 'firebase/app';
import { getAuth, signInAnonymously, signInWithCustomToken, onAuthStateChanged, User } from 'firebase/auth';
import { getFirestore, onSnapshot, collection, query, where, doc, updateDoc, Firestore } from 'firebase/firestore';

// Imports RxJS necessários para o template (async pipe e BehaviorSubject)
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { HttpClientModule } from '@angular/common/http';

// Declaração de variáveis globais (para o compilador TS)
declare const __app_id: string;
declare const __firebase_config: string;
declare const __initial_auth_token: string;

// Alert interface definition (Must match Alert.java)
interface Alert {
  id: string;
  timestamp: number; 
  endpointId: string;
  userId: string;
  mlScore: number;
  cognitiveAnalysis: string;
  auraConfidenceScore: number;
  status: 'PENDENTE' | 'MITIGADO' | 'RISCO_REAL';
}

// --- i18n TRANSLATION DATA (A ser usado no template) ---
const TRANSLATIONS: { [key: string]: { [key: string]: string } } = {
  'en': {
    'APP_TITLE': 'AURA Sentinel: Cognitive Threat Defense',
    'STATUS_GLOBAL': 'Global Status: Cognitive Defense',
    'MODE_MONITOR': 'Current Mode: MONITORING (Alert Only)',
    'MODE_ENFORCEMENT': 'Current Mode: ENFORCEMENT (Automatic Isolation Enabled)',
    'TOGGLE_MODE': 'Toggle Action Mode:',
    'TOGGLE_ON': 'ON',
    'TOGGLE_OFF': 'OFF',
    'NOTE_OFF': 'Alerts only. Manual action required.',
    'NOTE_ON': 'Automatic isolation for AURA Score > 95%.',
    'ALERTS_PENDING': 'Pending Alerts for Validation',
    'RISK_REAL': 'Confirmed Real Risk',
    'EVENTS_TOTAL': 'Total Events Since Install',
    'ACTION_REQUIRED': 'Action Required',
    'TABLE_CONFIDENCE': 'AURA Confidence (AI)',
    'TABLE_ENDPOINT': 'Endpoint / User',
    'TABLE_ANALYSIS': 'Cognitive Analysis (Gemini)',
    'TABLE_ACTION': 'Recommended Action',
    'EMPTY_ALERT': 'No pending alerts. Systems are in a normal behavioral state.',
    'MITIGATE_BTN': 'Mitigate',
    'CONFIRM_BTN': 'Confirm Risk',
    'USER_ID': 'User ID:',
    'CONNECTING': 'Connecting to Firestore...',
    'ISOLATION_NOTICE': 'ATTENTION: Disabling Enforcement means AURA Sentinel will NOT automatically isolate machines, even in case of Confirmed Real Risk. Proceed?',
    'LANG_SWITCH': 'Language:',
    'RISK_CRITICAL': 'Critical Risk',
    'RISK_LOW': 'Low Risk',
    'RISK_MITIGATED': 'Mitigated',
    'HISTORY_TITLE': 'Response History (Mitigated / Critical)' // Chave adicionada
  },
  'es': {
    'APP_TITLE': 'AURA Sentinel: Defensa Cognitiva contra Amenazas',
    'STATUS_GLOBAL': 'Estado Global: Defensa Cognitiva',
    'MODE_MONITOR': 'Modo Actual: MONITORIZACIÓN (Solo Alerta)',
    'MODE_ENFORCEMENT': 'Modo Actual: APLICACIÓN (Aislamiento Automático Habilitado)',
    'TOGGLE_MODE': 'Alternar Modo de Acción:',
    'TOGGLE_ON': 'ACTIVO',
    'TOGGLE_OFF': 'INACTIVO',
    'NOTE_OFF': 'Solo alertas. Se requiere acción manual.',
    'NOTE_ON': 'Aislamiento automático para Puntuación AURA > 95%.',
    'ALERTS_PENDING': 'Alertas Pendientes de Validación',
    'RISK_REAL': 'Riesgo Real Confirmado',
    'EVENTS_TOTAL': 'Total de Eventos Desde la Instalación',
    'ACTION_REQUIRED': 'Acción Requerida',
    'TABLE_CONFIDENCE': 'Confianza AURA (IA)',
    'TABLE_ENDPOINT': 'Punto Final / Usuario',
    'TABLE_ANALYSIS': 'Análisis Cognitivo (Gemini)',
    'TABLE_ACTION': 'Acción Recomendada',
    'EMPTY_ALERT': 'No hay alertas pendientes. Los sistemas están en um estado de comportamiento normal.',
    'MITIGATE_BTN': 'Mitigar',
    'CONFIRM_BTN': 'Confirmar Riesgo',
    'USER_ID': 'ID de Usuario:',
    'CONNECTING': 'Conectando a Firestore...',
    'ISOLATION_NOTICE': 'ATENCIÓN: Desactivar la Aplicación significa que AURA Sentinel NO aislará máquinas automáticamente, incluso en caso de Riesgo Real Confirmado. ¿Continuar?',
    'LANG_SWITCH': 'Idioma:',
    'RISK_CRITICAL': 'Riesgo Crítico',
    'RISK_LOW': 'Riesgo Bajo',
    'RISK_MITIGATED': 'Mitigado',
    'HISTORY_TITLE': 'Histórico de Respuesta (Mitigados / Críticos)' // Chave adicionada
  }
};

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html', // Aponta para o arquivo HTML separado
  styleUrls: ['./app.component.css'], 
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnInit, OnDestroy { // AQUI: Exportado e nomeado como AppComponent
  // Global Variables for Firebase
  private appId: string = typeof __app_id !== 'undefined' ? __app_id : 'default-app-id';
  private firebaseConfig: any = typeof __firebase_config !== 'undefined' ? JSON.parse(__firebase_config) : {};
  private initialAuthToken: string | null = typeof __initial_auth_token !== 'undefined' ? __initial_auth_token : null;
  private unsubscribeSnapshot: (() => void) | null = null;
  private unsubscribeConfig: (() => void) | null = null;

  // Application States (BehaviorSubject/Observables for Data Binding)
  db: Firestore | null = null;
  userId: string | null = null;
  language: string = 'en'; // Default language

  // Data Observables for the Template
  private todosAlertasSubject: BehaviorSubject<Alert[]> = new BehaviorSubject<Alert[]>([]);
  todosAlertas: Observable<Alert[]> = this.todosAlertasSubject.asObservable();
  
  // Computed Properties (Usando RxJS)
  alertasPendentes: Observable<Alert[]> = this.todosAlertas.pipe(
    map((alerts: Alert[]) => alerts.filter((a: Alert) => a.status === 'PENDENTE'))
  );
  alertasRiscoReal: Observable<Alert[]> = this.todosAlertas.pipe(
    map((alerts: Alert[]) => alerts.filter((a: Alert) => a.status === 'RISCO_REAL'))
  );
  alertasHistorico: Observable<Alert[]> = this.todosAlertas.pipe(
    map((alerts: Alert[]) => alerts.filter((a: Alert) => a.status !== 'PENDENTE'))
  );
  highestAuraScore: Observable<string> = this.todosAlertas.pipe(
    map((alerts: Alert[]) => {
      const highAlerts = alerts.filter(a => a.auraConfidenceScore >= 0.7);
      if (highAlerts.length === 0) return '0';
      const maxScore = Math.max(...highAlerts.map(a => a.auraConfidenceScore));
      return (maxScore * 100).toFixed(0);
    })
  );

  // State Properties for Two-Way Binding (Language Select) and simple state (Toggle)
  modoEnforce: boolean = false; 

  // i18n Translation Function (Membro da Classe)
  t = (key: string): string => TRANSLATIONS[this.language][key] || key;

  ngOnInit() {
    this.iniciarFirebase();
  }
  
  ngOnDestroy() {
    if (this.unsubscribeSnapshot) {
        this.unsubscribeSnapshot();
    }
    if (this.unsubscribeConfig) {
        this.unsubscribeConfig();
    }
  }

  async iniciarFirebase(): Promise<void> {
    try {
      const app = initializeApp(this.firebaseConfig);
      this.db = getFirestore(app);
      const auth = getAuth(app);
      
      // 1. Authentication
      if (this.initialAuthToken) {
        await signInWithCustomToken(auth, this.initialAuthToken);
      } else {
        await signInAnonymously(auth);
      }

      // 2. Auth State Observer (Tipagem corrigida para User | null)
      onAuthStateChanged(auth, (user: User | null) => {
        const uid = user ? user.uid : 'anon-' + Math.random().toString(36).substring(2, 10);
        this.userId = uid;

        // 3. Start Firestore Listeners after authentication
        if (this.db) {
          this.listenToConfig(uid);
          this.listenToAlerts();
        }
      });
    } catch (error) {
      console.error("Error initializing Firebase:", error);
    }
  }

  // Monitor the Enforcement Mode configuration (Tipagem corrigida)
  listenToConfig(uid: string): void {
    if (!this.db) return;
    const configDocRef = doc(this.db, `artifacts/${this.appId}/users/${uid}/config/global`);

    if (this.unsubscribeConfig) this.unsubscribeConfig();
    this.unsubscribeConfig = onSnapshot(configDocRef, (docSnap: any) => {
      if (docSnap.exists()) {
        const config = docSnap.data();
        this.modoEnforce = !!config['enforcementMode'];
      } else {
        this.updateConfig({ enforcementMode: false });
      }
    });
  }

  // Monitor the Alerts collection in real-time
  listenToAlerts(): void {
    if (!this.db) return;
    const alertsCollectionRef = collection(this.db, `artifacts/${this.appId}/public/data/alerts`);
    const q = query(alertsCollectionRef, where('appId', '==', this.appId)); 

    if (this.unsubscribeSnapshot) {
        this.unsubscribeSnapshot();
    }
    
    this.unsubscribeSnapshot = onSnapshot(q, (snapshot: any) => {
      const alerts: Alert[] = [];
      snapshot.forEach((doc: any) => {
        const data = doc.data();
        alerts.push({
          id: doc.id,
          timestamp: (data['creationTimestamp'] || Date.now()),
          endpointId: data['endpointId'] || 'N/A',
          userId: data['userId'] || 'system',
          mlScore: data['mlScore'] || 0.0,
          cognitiveAnalysis: data['cognitiveAnalysis'] || 'AI Analysis pending...', // MANTIDO PARA DEBUG
          auraConfidenceScore: data['auraConfidenceScore'] || 0.0,
          status: data['status'] as Alert['status'] || 'PENDENTE',
        });
      });
      alerts.sort((a, b) => b.auraConfidenceScore - a.auraConfidenceScore);
      this.todosAlertasSubject.next(alerts); // Atualiza o Subject
    }, (error: any) => {
      console.error("Error listening to alerts:", error);
    });
  }

  // --- User Actions ---

  setLanguage(event: any): void {
    const lang = event.target.value;
    if (TRANSLATIONS[lang]) {
      this.language = lang;
    }
  }
  
  toggleModoEnforcement(): void {
    const newMode = !this.modoEnforce;
    
    if (!newMode) {
      console.warn(this.t('ISOLATION_NOTICE')); 
      this.updateConfig({ enforcementMode: newMode });
    } else {
      this.updateConfig({ enforcementMode: newMode });
    }
  }

  // Updates the user configuration in Firestore (método assíncrono tipado)
  async updateConfig(data: any): Promise<void> {
    if (!this.db || !this.userId) return;
    const configDocRef = doc(this.db, `artifacts/${this.appId}/users/${this.userId}/config/global`);
    try {
      await updateDoc(configDocRef, data);
    } catch (e) {
      console.error("Failed to update config:", e);
    }
  }

  // The Manager confirms the risk is low/resolved (método assíncrono tipado)
  async marcarComoMitigado(alerta: Alert): Promise<void> {
    if (!this.db) return;
    const alertaDocRef = doc(this.db, `artifacts/${this.appId}/public/data/alerts/${alerta.id}`);
    try {
      await updateDoc(alertaDocRef, { status: 'MITIGADO' });
    } catch (e) {
      console.error("Error mitigating alert:", e);
    }
  }

  // The Manager confirms the risk is a real, high-priority incident (método assíncrono tipado)
  async marcarComoRiscoReal(alerta: Alert): Promise<void> {
    if (!this.db) return;
    const alertaDocRef = doc(this.db, `artifacts/${this.appId}/public/data/alerts/${alerta.id}`);
    try {
      await updateDoc(alertaDocRef, { status: 'RISCO_REAL' });
    } catch (e) {
      console.error("Error confirming real risk:", e);
    }
  }
}