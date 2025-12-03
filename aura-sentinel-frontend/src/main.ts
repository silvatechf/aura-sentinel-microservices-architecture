// Este arquivo é o ponto de entrada da aplicação
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { AppModule } from './app/app.module';

// Declaração de variáveis globais (necessário para o compilador TS)
declare const __app_id: string;
declare const __firebase_config: string;
declare const __initial_auth_token: string;

// Inicia o módulo raiz (AppModule)
platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));