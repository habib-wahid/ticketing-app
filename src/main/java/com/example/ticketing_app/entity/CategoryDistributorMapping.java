package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "category_distributor_mappings")
@Getter
@Setter
@NoArgsConstructor
public class CategoryDistributorMapping {

	@Id
	private String id;

	@Indexed(unique = true)
	private String categoryId;

	private String categoryName;

	private String distributorUserId;

	private String distributorName;

	private boolean active = true;

	private String createdBy;

	private LocalDateTime createdAt;

	private String updatedBy;

	private LocalDateTime updatedAt;
}
