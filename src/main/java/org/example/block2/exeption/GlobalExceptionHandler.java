package org.example.block2.exeption;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles validation errors.
     *
     * @param ex validation exception
     * @return problem details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Validation failed");

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        problemDetail.setDetail(errors);

        return problemDetail;
    }

    /**
     * Handles resource not found errors.
     *
     * @param ex resource not found exception
     * @return problem details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setTitle("Resource not found");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }

    /**
     * Handles duplicate resource errors.
     *
     * @param ex duplicate resource exception
     * @return problem details
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(
            DuplicateResourceException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problemDetail.setTitle("Resource already exists");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }

    /**
     * Handles constraint violations.
     *
     * @param ex constraint violation exception
     * @return problem details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Validation failed");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }
}
