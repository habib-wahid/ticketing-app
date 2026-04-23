package com.example.ticketing_app.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

	@Id
	private String id;

	@Indexed
	private String userId;

	@Indexed(unique = true)
	private String tokenHash;

	private Instant createdAt;
	private Instant expiresAt;
	private Instant revokedAt;
}

