package com.example.ticketing_app.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.RefreshToken;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
	Optional<RefreshToken> findByTokenHash(String tokenHash);
}

