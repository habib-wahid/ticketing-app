package com.example.ticketing_app.security;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "security.jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

	@NotBlank
	@Size(min = 32)
	private String secret;

	@NotBlank
	private String issuer = "ticketing-app";

	@Min(1)
	private long accessTokenMinutes = 60;

	@Min(1)
	private long refreshTokenDays = 7;

	public Duration accessTokenDuration() {
		return Duration.ofMinutes(accessTokenMinutes);
	}

	public Duration refreshTokenDuration() {
		return Duration.ofDays(refreshTokenDays);
	}
}

