package com.example.ticketing_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app.file-storage")
@Validated
@Getter
@Setter
public class FileStorageProperties {

	@NotBlank
	private String uploadDirectory = "./uploads";

	@NotBlank
	private String publicUrlPrefix = "/api/files";

	@Min(1)
	private long maxFileSizeBytes = 10 * 1024 * 1024;
}
