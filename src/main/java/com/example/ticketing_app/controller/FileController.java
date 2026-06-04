package com.example.ticketing_app.controller;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing_app.service.FileStorageService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files")
public class FileController {

	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@GetMapping("/tickets/{ticketId}/{filename}")
	public ResponseEntity<Resource> downloadTicketAttachment(@PathVariable String ticketId,
			@PathVariable String filename) throws Exception {
		Path filePath = fileStorageService.resolveTicketFile(ticketId, filename);
		String contentType = Files.probeContentType(filePath);
		if (contentType == null) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
		Resource resource = new FileSystemResource(filePath);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
				.body(resource);
	}
}
