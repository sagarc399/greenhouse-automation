package com.greenhouse.app.exception;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Centralized exception handler for all REST controllers.
 *
 * <p>Translates application exceptions into RFC 7807 {@link ProblemDetail} responses
 * with consistent structure and i18n support.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Constructs the handler with a message source for i18n error messages.
     *
     * @param messageSource Spring MessageSource for locale-aware messages
     */
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Handles {@link ResourceNotFoundException} and returns HTTP 404.
     *
     * @param ex     the exception
     * @param locale the request locale for i18n
     * @return a 404 ProblemDetail
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, Locale locale) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Resource Not Found");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Handles Bean Validation errors ({@code @Valid}) and returns HTTP 400
     * with a map of field-level error messages.
     *
     * @param ex     the validation exception
     * @param locale the request locale for i18n
     * @return a 400 ProblemDetail with a {@code violations} property
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> violations = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            violations.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.bad.request", new Object[]{"validation failed"}, locale)
        );
        detail.setTitle("Validation Error");
        detail.setProperty("violations", violations);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Handles {@link IllegalArgumentException} and returns HTTP 400.
     *
     * @param ex     the exception
     * @param locale the request locale for i18n
     * @return a 400 ProblemDetail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, Locale locale) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Bad Request");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Catch-all handler for any unhandled exception, returning HTTP 500.
     *
     * @param ex     the exception
     * @param locale the request locale for i18n
     * @return a 500 ProblemDetail
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, Locale locale) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageSource.getMessage("error.internal", null, locale)
        );
        detail.setTitle("Internal Server Error");
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }
}
