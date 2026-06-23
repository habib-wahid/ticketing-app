package com.example.ticketing_app.service.sla;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.ticketing_app.entity.Ticket;
import com.example.ticketing_app.entity.TicketPriority;
import com.example.ticketing_app.entity.TicketStatus;

class SlaClockTest {

	private SlaClock slaClock;

	@BeforeEach
	void setUp() {
		SlaCalendarConfig config = new SlaCalendarConfig();
		slaClock = new SlaClock(config, Clock.systemDefaultZone());
	}

	@Test
	void mediumTicketOnThursdayEveningSkipsWeekend() {
		// Thu 2026-06-18 17:00 + 8 working hours -> Sun 2026-06-21 16:00
		LocalDateTime start = LocalDateTime.of(2026, 6, 18, 17, 0);
		SlaClockTargets targets = new SlaClockTargets(8, 8, 4, 2, false);

		SlaDeadlines deadlines = slaClock.computeDeadlines(start, targets);

		assertEquals(LocalDateTime.of(2026, 6, 21, 16, 0), deadlines.slaDeadline());
	}

	@Test
	void mediumTicketOnFridayStartsOnSunday() {
		// Fri 2026-06-19 10:00 + 8 working hours -> Sun 17:00
		LocalDateTime start = LocalDateTime.of(2026, 6, 19, 10, 0);
		SlaClockTargets targets = new SlaClockTargets(8, 8, 4, 2, false);

		SlaDeadlines deadlines = slaClock.computeDeadlines(start, targets);

		assertEquals(LocalDateTime.of(2026, 6, 21, 17, 0), deadlines.slaDeadline());
	}

	@Test
	void criticalTicketUsesTwentyFourSevenClock() {
		LocalDateTime start = LocalDateTime.of(2026, 6, 18, 22, 0);
		SlaClockTargets targets = SlaClockTargets.defaultFor(TicketPriority.CRITICAL);

		SlaDeadlines deadlines = slaClock.computeDeadlines(start, targets);

		assertEquals(start.plusHours(4), deadlines.slaDeadline());
	}

	@Test
	void pauseFreezesRemainingMinutes() {
		Ticket ticket = new Ticket();
		ticket.setPriority(TicketPriority.MEDIUM);
		ticket.setStatus(TicketStatus.WAITING_ON_CUSTOMER);
		ticket.setSlaDeadline(LocalDateTime.of(2026, 6, 21, 16, 0));
		ticket.setPausedAt(LocalDateTime.of(2026, 6, 18, 12, 0));

		long remainingAtPause = slaClock.remainingMinutes(LocalDateTime.of(2026, 6, 19, 12, 0), ticket);
		long remainingLater = slaClock.remainingMinutes(LocalDateTime.of(2026, 6, 20, 12, 0), ticket);

		assertEquals(remainingAtPause, remainingLater);
		assertTrue(remainingAtPause > 0);
	}

	@Test
	void resolutionBreachedWhenUnresolvedPastSlaDeadline() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.IN_PROGRESS);
		ticket.setSlaDeadline(LocalDateTime.of(2026, 6, 18, 12, 0));

		assertTrue(slaClock.isBreached(LocalDateTime.of(2026, 6, 18, 12, 1), ticket));
	}

	@Test
	void resolutionNotBreachedWhenResolvedBeforeDeadline() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.RESOLVED);
		ticket.setSlaDeadline(LocalDateTime.of(2026, 6, 18, 12, 0));

		assertTrue(!slaClock.isBreached(LocalDateTime.of(2026, 6, 18, 13, 0), ticket));
	}

	@Test
	void resolutionNotBreachedWhilePausedPastDeadline() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.WAITING_ON_CUSTOMER);
		ticket.setSlaDeadline(LocalDateTime.of(2026, 6, 18, 12, 0));
		ticket.setPausedAt(LocalDateTime.of(2026, 6, 18, 11, 0));

		assertTrue(!slaClock.isBreached(LocalDateTime.of(2026, 6, 18, 13, 0), ticket));
	}

	@Test
	void firstResponseMinutesUsesWorkingTimeForMediumPriority() {
		Ticket ticket = new Ticket();
		ticket.setPriority(TicketPriority.MEDIUM);
		ticket.setCreatedAt(LocalDateTime.of(2026, 6, 18, 17, 0));
		LocalDateTime assignedAt = LocalDateTime.of(2026, 6, 19, 10, 0);

		long minutes = slaClock.calculateFirstResponseMinutes(ticket, assignedAt);

		assertEquals(60, minutes);
	}
}
