package com.example.ticketing_app.service.sla;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.sla")
@Getter
@Setter
public class SlaCalendarConfig {

	private BusinessHours businessHours = new BusinessHours();
	private List<String> holidays = new ArrayList<>();

	@Getter
	@Setter
	public static class BusinessHours {
		private String timezone = "Asia/Dhaka";
		private String start = "09:00";
		private String end = "18:00";
		private List<DayOfWeek> workingDays = List.of(
				DayOfWeek.SUNDAY,
				DayOfWeek.MONDAY,
				DayOfWeek.TUESDAY,
				DayOfWeek.WEDNESDAY,
				DayOfWeek.THURSDAY);
	}

	public LocalTime windowStart() {
		return LocalTime.parse(businessHours.getStart());
	}

	public LocalTime windowEnd() {
		return LocalTime.parse(businessHours.getEnd());
	}

	public Set<DayOfWeek> workingDaysSet() {
		return new HashSet<>(businessHours.getWorkingDays());
	}

	public Set<LocalDate> holidayDates() {
		Set<LocalDate> dates = new HashSet<>();
		for (String holiday : holidays) {
			if (holiday != null && !holiday.isBlank()) {
				dates.add(LocalDate.parse(holiday.trim()));
			}
		}
		return dates;
	}
}
