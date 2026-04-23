package com.example.ticketing_app.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.ticketing_app.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiResponse<Object>> handleConflict(ConflictException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({BadRequestException.class, MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiResponse<Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
		String message = ex instanceof MethodArgumentNotValidException validException
				? validException.getBindingResult().getFieldErrors().stream()
						.map(error -> error.getField() + " " + error.getDefaultMessage())
						.findFirst()
						.orElse("Validation failed")
				: ex.getMessage();
		return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiResponse<Object>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI());
	}

	private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String message, String path) {
		ApiResponse<Object> response = ApiResponse.failure(message, status.value(), status.getReasonPhrase(), path);
		return ResponseEntity.status(status).body(response);
	}
}
