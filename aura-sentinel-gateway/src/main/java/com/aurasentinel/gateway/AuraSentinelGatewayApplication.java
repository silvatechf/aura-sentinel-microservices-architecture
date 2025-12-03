package com.aurasentinel.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// Habilita o processamento assíncrono para os métodos marcados com @Async
// Isso garante que a IngestionService não bloqueie a fila de requisições do Controller.
@EnableAsync 
@SpringBootApplication
public class AuraSentinelGatewayApplication {

	public static void main(String[] args) {
		// Inicia o serviço Gateway
		SpringApplication.run(AuraSentinelGatewayApplication.class, args);
	}

}