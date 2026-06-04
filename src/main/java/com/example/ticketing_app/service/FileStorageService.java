package com.example.ticketing_app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.ticketing_app.config.FileStorageProperties;
import com.example.ticketing_app.exception.BadRequestException;
import com.example.ticketing_app.exception.ResourceNotFoundException;

@Service
public class FileStorageService {

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "jpg", "jpeg", "png");

	private final Path uploadRoot;
	private final String publicUrlPrefix;
	private final long maxFileSizeBytes;

	public FileStorageService(FileStorageProperties properties) {
		this.uploadRoot = Paths.get(properties.getUploadDirectory()).toAbsolutePath().normalize();
		this.publicUrlPrefix = normalizePrefix(properties.getPublicUrlPrefix());
		this.maxFileSizeBytes = properties.getMaxFileSizeBytes();
	}

	public StoredFile storeTicketAttachment(String ticketId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Attachment file is required");
		}
		if (file.getSize() > maxFileSizeBytes) {
			throw new BadRequestException("File size must not exceed 10 MB");
		}

		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
		if (!StringUtils.hasText(originalFilename) || originalFilename.contains("..")) {
			throw new BadRequestException("Invalid file name");
		}

		String extension = extractExtension(originalFilename);
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new BadRequestException("Unsupported file format. Allowed: pdf, doc, jpg, png");
		}

		String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
		Path ticketDirectory = uploadRoot.resolve("tickets").resolve(ticketId);
		Path targetPath = ticketDirectory.resolve(storedFilename).normalize();

		if (!targetPath.startsWith(uploadRoot)) {
			throw new BadRequestException("Invalid file path");
		}

		try {
			Files.createDirectories(ticketDirectory);
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			throw new BadRequestException("Failed to store file: " + ex.getMessage());
		}

		String accessiblePath = publicUrlPrefix + "/tickets/" + ticketId + "/" + storedFilename;
		String mimeType = resolveMimeType(extension, file.getContentType());
		return new StoredFile(originalFilename, storedFilename, accessiblePath, file.getSize(), mimeType);
	}

	public Path resolveTicketFile(String ticketId, String filename) {
		if (!StringUtils.hasText(ticketId) || !StringUtils.hasText(filename) || filename.contains("..")) {
			throw new BadRequestException("Invalid file request");
		}
		Path filePath = uploadRoot.resolve("tickets").resolve(ticketId).resolve(filename).normalize();
		if (!filePath.startsWith(uploadRoot) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			throw new ResourceNotFoundException("File not found");
		}
		return filePath;
	}

	private String extractExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == filename.length() - 1) {
			throw new BadRequestException("File must have an extension");
		}
		return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}

	private String resolveMimeType(String extension, String providedContentType) {
		if (StringUtils.hasText(providedContentType)) {
			return providedContentType;
		}
		return switch (extension) {
			case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
			case "doc" -> "application/msword";
			case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
			case "png" -> MediaType.IMAGE_PNG_VALUE;
			default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
		};
	}

	private String normalizePrefix(String prefix) {
		String normalized = prefix == null ? "" : prefix.trim();
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}
		if (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized;
	}

	public record StoredFile(String originalFilename, String storedFilename, String accessiblePath, long fileSize, String mimeType) {
	}
}
