package com.example.ticketing_app.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TicketAttachment {

	private String attachmentId;
	private String filename;
	private String s3Url;
	private long fileSize;
	private String mimeType;
	private String uploadedBy;
	private LocalDateTime uploadedAt;
}

