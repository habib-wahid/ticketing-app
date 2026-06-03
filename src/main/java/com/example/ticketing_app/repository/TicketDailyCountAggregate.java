package com.example.ticketing_app.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDailyCountAggregate {
    private String date;
    private long count;
}

