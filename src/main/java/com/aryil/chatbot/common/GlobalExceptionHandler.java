package com.aryil.chatbot.common;

import com.aryil.chatbot.common.dto.ApiError;
import com.aryil.chatbot.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        return ApiError.of(
                request.getRequestURI(),
                "VALIDATION_ERROR",
                "Validation failed",
                details);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "INVALID_CREDENTIALS",
                getMessage("error.auth.invalid_credentials"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "EMAIL_EXISTS",
                getMessage("error.auth.email_exists"));
    }

    @ExceptionHandler(ConversationAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleConversationAccess(ConversationAccessException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "ACCESS_DENIED",
                getMessage("error.chat.not_your_conversation"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "ACCESS_DENIED",
                "Access denied");
    }

    @ExceptionHandler(PatternDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlePatternDuplicate(PatternDuplicateException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "PATTERN_DUPLICATE",
                getMessage("error.policy.pattern_duplicate"));
    }

    @ExceptionHandler(PatternNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handlePatternNotFound(PatternNotFoundException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "PATTERN_NOT_FOUND",
                getMessage("error.policy.pattern_not_found"));
    }

    @ExceptionHandler(ContentBlockedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleContentBlocked(ContentBlockedException ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "CONTENT_BLOCKED",
                getMessage("error.content.blocked"),
                Map.of("reason", ex.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && message.contains("banned_patterns_pattern_key")) {
            return ApiError.of(
                    request.getRequestURI(),
                    "PATTERN_DUPLICATE",
                    getMessage("error.policy.pattern_duplicate"));
        }
        return ApiError.of(
                request.getRequestURI(),
                "DATA_INTEGRITY_ERROR",
                "Data integrity violation");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(Exception ex, HttpServletRequest request) {
        return ApiError.of(
                request.getRequestURI(),
                "INTERNAL_ERROR",
                "An unexpected error occurred");
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

}
