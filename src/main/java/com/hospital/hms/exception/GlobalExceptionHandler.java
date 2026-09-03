package com.hospital.hms.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Centralised exception handling for the whole application.
 * Renders professional, user-friendly error pages instead of stack traces.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        log.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error/404";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResource(DuplicateResourceException ex, Model model, HttpServletRequest request) {
        log.warn("Duplicate resource on {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/general";
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOperationNotAllowed(OperationNotAllowedException ex, Model model, HttpServletRequest request) {
        log.warn("Operation not allowed on {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 409);
        return "error/general";
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Exception ex, Model model, HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("errorMessage", "You do not have permission to access this resource.");
        model.addAttribute("statusCode", 403);
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("statusCode", 500);
        return "error/general";
    }
}
