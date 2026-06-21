package com.example.ticketing_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.ticketing_app.dto.ComplaintCategorySummaryResponse;
import com.example.ticketing_app.dto.SlaPolicyCreateRequest;
import com.example.ticketing_app.dto.SlaPolicyResponse;
import com.example.ticketing_app.dto.SlaPolicyUpdateRequest;
import com.example.ticketing_app.entity.SlaPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SlaPolicyMapper {

	@Mapping(target = "category", expression = "java(toCategorySummary(policy))")
	SlaPolicyResponse toResponse(SlaPolicy policy);

	List<SlaPolicyResponse> toResponseList(List<SlaPolicy> policies);

	default ComplaintCategorySummaryResponse toCategorySummary(SlaPolicy policy) {
		if (policy == null || policy.getCategoryId() == null) {
			return null;
		}
		return new ComplaintCategorySummaryResponse(policy.getCategoryId(), policy.getCategoryName());
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "categoryId", ignore = true)
	@Mapping(target = "categoryName", ignore = true)
	@Mapping(target = "active", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	SlaPolicy toEntity(SlaPolicyCreateRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "categoryId", ignore = true)
	@Mapping(target = "categoryName", ignore = true)
	@Mapping(target = "priority", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "firstResponseTimeHours", source = "responseTimeHours")
	@Mapping(target = "reminderThreshHoldHours", source = "reminderIntervalMinutes")
	void updateEntity(SlaPolicyUpdateRequest request, @MappingTarget SlaPolicy policy);
}
