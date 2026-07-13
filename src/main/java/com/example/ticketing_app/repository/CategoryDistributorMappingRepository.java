package com.example.ticketing_app.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.ticketing_app.entity.CategoryDistributorMapping;

public interface CategoryDistributorMappingRepository extends MongoRepository<CategoryDistributorMapping, String> {

	Optional<CategoryDistributorMapping> findByCategoryId(String categoryId);

	boolean existsByCategoryId(String categoryId);
}
