package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "complaint_categories")
@Getter
@Setter
@NoArgsConstructor
public class ComplaintCategory {

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private String createdBy;
	private LocalDateTime createdAt;
	private String updatedBy;
	private LocalDateTime updatedAt;
}

