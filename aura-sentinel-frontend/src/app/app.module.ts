import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // CRÍTICO: Fornece o *ngIf e *ngFor

import { AppComponent } from './app.component'; // Nosso componente
import { HttpClientModule } from '@angular/common/http'; // Necessário para futura comunicação HTTP

@NgModule({
  // Declaramos nosso componente principal
  declarations: [
    AppComponent
  ],
  // Importamos todos os módulos necessários para o template funcionar
  imports: [
    BrowserModule,
    FormsModule, // Para [(ngModel)] no seletor de idioma
    CommonModule, // Resolve o erro NG0201
    HttpClientModule // Para APIs HTTP
  ],
  providers: [],
  // Iniciamos a aplicação com o AppComponent
  bootstrap: [AppComponent]
})
export class AppModule { }