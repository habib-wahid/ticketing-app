package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "sla_policies")
@CompoundIndexes({
		// A policy is uniquely identified by its complaint category + priority pair
		@CompoundIndex(name = "idx_category_priority", def = "{'categoryId': 1, 'priority': 1}", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class SlaPolicy {

	@Id
	private String id;

	private String name;
	private String categoryId;
	private String categoryName;
	private TicketPriority priority;

	private Integer firstResponseTimeHours;
	private Integer resolutionTimeHours;
	private Integer escalationAfterHours;
	private Integer reminderThreshHoldHours;
	private boolean active = true;
	private String updatedBy;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;

}
