package com.example.ticketing_app.service.sla;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketStatus;

@Component
public class SlaClock {

	private final SlaCalendarConfig calendar;
	private final Clock clock;

	public SlaClock(SlaCalendarConfig calendar, Clock clock) {
		this.calendar = calendar;
		this.clock = clock;
	}

	public SlaDeadlines computeDeadlines(LocalDateTime start, SlaClockTargets targets) {
		LocalDateTime responseDeadline = addWorkingDuration(start, targets.firstResponseTimeHours(),
				targets.twentyFourSeven());
		LocalDateTime slaDeadline = addWorkingDuration(start, targets.resolutionTimeHours(), targets.twentyFourSeven());
		LocalDateTime escalationDueAt = addWorkingDuration(start, targets.escalationAfterHours(),
				targets.twentyFourSeven());

		LocalDateTime nextReminderAt = slaDeadline.minusHours(targets.reminderThresholdHours());
		if (nextReminderAt.isBefore(start)) {
			nextReminderAt = start;
		}

		return new SlaDeadlines(responseDeadline, slaDeadline, escalationDueAt, nextReminderAt);
	}

	public long remainingMinutes(LocalDateTime now, Ticket ticket) {
		if (ticket.getSlaDeadline() == null || isTerminal(ticket.getStatus())) {
			return 0;
		}

		LocalDateTime effectiveNow = ticket.getPausedAt() != null ? ticket.getPausedAt() : now;
		boolean twentyFourSeven = SlaClockTargets.isTwentyFourSeven(ticket.getPriority());
		long remaining = workingMinutesBetween(effectiveNow, ticket.getSlaDeadline(), twentyFourSeven);
		return Math.max(0, remaining);
	}

	public boolean isBreached(LocalDateTime now, Ticket ticket) {
		if (isTerminal(ticket.getStatus()) || ticket.getSlaDeadline() == null) {
			return false;
		}
		if (ticket.getPausedAt() != null) {
			return false;
		}
		return now.isAfter(ticket.getSlaDeadline());
	}

	public long calculateFirstResponseMinutes(Ticket ticket, LocalDateTime assignedAt) {
		if (ticket.getCreatedAt() == null || assignedAt == null) {
			return 0;
		}
		boolean twentyFourSeven = SlaClockTargets.isTwentyFourSeven(ticket.getPriority());
		return workingMinutesBetween(ticket.getCreatedAt(), assignedAt, twentyFourSeven);
	}

	public boolean isResponseBreached(LocalDateTime assignedAt, LocalDateTime responseDeadline) {
		return responseDeadline != null && assignedAt != null && assignedAt.isAfter(responseDeadline);
	}

	public boolean isResponseBreachedUnassigned(LocalDateTime now, Ticket ticket) {
		if (ticket.getAssignedAt() != null || ticket.getResponseDeadline() == null) {
			return false;
		}
		if (ticket.getPausedAt() != null) {
			return false;
		}
		return now.isAfter(ticket.getResponseDeadline());
	}

	public void onPause(Ticket ticket, LocalDateTime now) {
		if (ticket.getPausedAt() == null) {
			ticket.setPausedAt(now);
		}
	}

	public void onResume(Ticket ticket, LocalDateTime now) {
		if (ticket.getPausedAt() == null) {
			return;
		}

		boolean twentyFourSeven = SlaClockTargets.isTwentyFourSeven(ticket.getPriority());
		long pauseMinutes = workingMinutesBetween(ticket.getPausedAt(), now, twentyFourSeven);
		if (pauseMinutes > 0) {
			shiftDeadlines(ticket, pauseMinutes, twentyFourSeven);
			long accumulated = ticket.getPausedMinutesTotal() + pauseMinutes;
			ticket.setPausedMinutesTotal(accumulated);
		}
		ticket.setPausedAt(null);
	}

	public LocalDateTime addWorkingDuration(LocalDateTime start, int hours, boolean twentyFourSeven) {
		return addWorkingMinutes(start, hours * 60L, twentyFourSeven);
	}

	public LocalDateTime addWorkingMinutes(LocalDateTime start, long minutesToAdd, boolean twentyFourSeven) {
		if (twentyFourSeven) {
			return start.plusMinutes(minutesToAdd);
		}
		if (minutesToAdd <= 0) {
			return start;
		}

		long remaining = minutesToAdd;
		LocalDateTime cursor = normalizeToWorkingMinute(start);

		while (remaining > 0) {
			if (!isWorkingInstant(cursor)) {
				cursor = startOfNextWorkingDay(cursor);
				continue;
			}

			LocalDateTime windowEnd = windowEnd(cursor);
			long minutesLeftToday = Duration.between(cursor, windowEnd).toMinutes();
			if (minutesLeftToday <= 0) {
				cursor = startOfNextWorkingDay(cursor);
				continue;
			}

			long consume = Math.min(remaining, minutesLeftToday);
			cursor = cursor.plusMinutes(consume);
			remaining -= consume;

			if (remaining > 0) {
				cursor = startOfNextWorkingDay(cursor);
			}
		}

		return cursor;
	}

	public long workingMinutesBetween(LocalDateTime start, LocalDateTime end, boolean twentyFourSeven) {
		if (start == null || end == null || !end.isAfter(start)) {
			return 0;
		}
		if (twentyFourSeven) {
			return Duration.between(start, end).toMinutes();
		}

		long total = 0;
		LocalDateTime cursor = normalizeToWorkingMinute(start);

		while (cursor.isBefore(end)) {
			if (!isWorkingInstant(cursor)) {
				cursor = startOfNextWorkingDay(cursor);
				continue;
			}

			LocalDateTime windowEnd = windowEnd(cursor);
			LocalDateTime segmentEnd = end.isBefore(windowEnd) ? end : windowEnd;
			if (segmentEnd.isAfter(cursor)) {
				total += Duration.between(cursor, segmentEnd).toMinutes();
			}

			if (!end.isAfter(windowEnd)) {
				break;
			}
			cursor = startOfNextWorkingDay(cursor);
		}

		return total;
	}

	private void shiftDeadlines(Ticket ticket, long pauseMinutes, boolean twentyFourSeven) {
		if (ticket.getResponseDeadline() != null) {
			ticket.setResponseDeadline(addWorkingMinutes(ticket.getResponseDeadline(), pauseMinutes, twentyFourSeven));
		}
		if (ticket.getSlaDeadline() != null) {
			ticket.setSlaDeadline(addWorkingMinutes(ticket.getSlaDeadline(), pauseMinutes, twentyFourSeven));
		}
		if (ticket.getEscalationDueAt() != null) {
			ticket.setEscalationDueAt(addWorkingMinutes(ticket.getEscalationDueAt(), pauseMinutes, twentyFourSeven));
		}
		if (ticket.getNextReminderAt() != null) {
			ticket.setNextReminderAt(addWorkingMinutes(ticket.getNextReminderAt(), pauseMinutes, twentyFourSeven));
		}
	}

	private LocalDateTime normalizeToWorkingMinute(LocalDateTime instant) {
		if (!isWorkingInstant(instant)) {
			return startOfNextWorkingDay(instant);
		}

		LocalTime time = instant.toLocalTime();
		LocalTime start = calendar.windowStart();
		LocalTime end = calendar.windowEnd();

		if (time.isBefore(start)) {
			return instant.with(start);
		}
		if (!time.isBefore(end)) {
			return startOfNextWorkingDay(instant);
		}
		return instant;
	}

	private LocalDateTime startOfNextWorkingDay(LocalDateTime instant) {
		LocalDateTime candidate = instant.toLocalDate().plusDays(1).atTime(calendar.windowStart());
		while (!isWorkingInstant(candidate)) {
			candidate = candidate.plusDays(1);
		}
		return candidate;
	}

	private LocalDateTime windowEnd(LocalDateTime instant) {
		return instant.toLocalDate().atTime(calendar.windowEnd());
	}

	private boolean isWorkingInstant(LocalDateTime instant) {
		LocalDate date = instant.toLocalDate();
		if (calendar.holidayDates().contains(date)) {
			return false;
		}
		Set<DayOfWeek> workingDays = calendar.workingDaysSet();
		return workingDays.contains(date.getDayOfWeek());
	}

	private boolean isTerminal(TicketStatus status) {
		return status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED;
	}

	public LocalDateTime now() {
		return LocalDateTime.now(clock);
	}
}
