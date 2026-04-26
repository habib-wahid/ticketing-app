package com.example.ticketing_app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.ComplaintCategory;

public interface ComplaintCategoryRepository extends MongoRepository<ComplaintCategory, String> {
	boolean existsByNameIgnoreCase(String name);
}

