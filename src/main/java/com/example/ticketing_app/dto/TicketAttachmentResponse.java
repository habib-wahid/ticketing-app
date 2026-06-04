package com.example.ticketing_app.dto;

import java.time.LocalDateTime;

public record TicketAttachmentResponse(
		String attachmentId,
		String filename,
		String filePath,
		String s3Url,
		long fileSize,
		String mimeType,
		String uploadedBy,
		LocalDateTime uploadedAt) {
}

