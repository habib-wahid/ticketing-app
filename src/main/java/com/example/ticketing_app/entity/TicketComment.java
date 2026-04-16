package com.example.ticketing_app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketComment {

	private String commentId;
	private CommentAuthor author;
	private String text;
	private boolean internal;
	private List<String> attachments = new ArrayList<>();
	private LocalDateTime createdAt;
}

