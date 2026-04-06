package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

public record TicketSlaSummary(LocalDateTime deadline, long remainingMinutes, boolean breached) {
}

