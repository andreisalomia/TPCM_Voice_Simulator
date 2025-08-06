package com.example.tpcm_voice_simulator.exceptions;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackages = {"com.example.tpcm_voice.controller"})
@Hidden
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    protected ResponseEntity<Object> handleValidation(ValidationException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ConflictException.class)
    protected ResponseEntity<Object> handleConflict(ConflictException ex, WebRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, ex);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleIllegal(RuntimeException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Illegal argument", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex.getMessage(), request, ex);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message, WebRequest request, Exception ex) {
        ErrorMessageTemplate body = new ErrorMessageTemplate(status.value(), error, message);
        return handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }
}