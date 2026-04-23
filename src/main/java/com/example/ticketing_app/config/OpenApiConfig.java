package com.example.ticketing_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI ticketingOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Ticketing API")
						.version("1.0")
						.description("CRUD APIs for users, tickets, and SLA policies"))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.schemaRequirement(BEARER_SCHEME, new SecurityScheme()
						.name("Authorization")
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT"));
	}
}
