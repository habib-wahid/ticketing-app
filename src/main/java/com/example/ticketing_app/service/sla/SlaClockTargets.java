package com.example.ticketing_app.service.sla;

import com.example.ticketing_app.entity.SlaPolicy;
import com.example.ticketing_app.entity.TicketPriority;

public record SlaClockTargets(
		int firstResponseTimeHours,
		int resolutionTimeHours,
		int escalationAfterHours,
		int reminderThresholdHours,
		boolean twentyFourSeven) {

	public static SlaClockTargets from(SlaPolicy policy, TicketPriority priority) {
		if (policy != null && policy.isActive()) {
			return new SlaClockTargets(
					policy.getFirstResponseTimeHours(),
					policy.getResolutionTimeHours(),
					policy.getEscalationAfterHours(),
					policy.getReminderThreshHoldHours(),
					isTwentyFourSeven(priority));
		}
		return defaultFor(priority);
	}

	public static SlaClockTargets defaultFor(TicketPriority priority) {
		return switch (priority) {
			case LOW -> new SlaClockTargets(24, 72, 48, 2, false);
			case MEDIUM -> new SlaClockTargets(8, 24, 12, 2, false);
			case HIGH -> new SlaClockTargets(2, 8, 4, 1, true);
			case CRITICAL -> new SlaClockTargets(1, 4, 2, 1, true);
		};
	}

	public static boolean isTwentyFourSeven(TicketPriority priority) {
		return priority == TicketPriority.CRITICAL || priority == TicketPriority.HIGH;
	}
}
