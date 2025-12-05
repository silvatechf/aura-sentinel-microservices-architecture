import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; 

import { AppComponent } from './app.component'; 
import { HttpClientModule } from '@angular/common/http'; 

@NgModule({
  // Main component
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule, 
    CommonModule, 
    HttpClientModule 
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
