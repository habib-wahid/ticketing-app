package com.example.ticketing_app.dto;

import java.time.OffsetDateTime;

public record ApiResponse<C>(
		boolean success,
		String message,
		C data,
		ApiError error,
		OffsetDateTime timestamp) {

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data, null, OffsetDateTime.now());
	}

	public static <T> ApiResponse<T> failure(String message, int status, String error, String path) {
		ApiError apiError = new ApiError(status, error, path);
		return new ApiResponse<>(false, message, null, apiError, OffsetDateTime.now());
	}

	public record ApiError(int status, String error, String path) {
	}
}

