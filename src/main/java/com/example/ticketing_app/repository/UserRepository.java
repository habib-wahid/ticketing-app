package com.example.ticketing_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.User;

public interface UserRepository extends MongoRepository<User, String> {

	Optional<User> findByUserId(String userId);
	
	List<User> findByUserIdIn(List<String> userIds);

	Optional<User> findByEmail(String email);

	boolean existsByUserId(String userId);

	boolean existsByEmail(String email);
}
