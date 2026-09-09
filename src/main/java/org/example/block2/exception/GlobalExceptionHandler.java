package org.example.block2.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for REST API.
 */
@Slf4j
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
    /**
     * Handles database constraint and data integrity violations (e.g., foreign key conflicts).
     *
     * @param ex data integrity violation exception
     * @return problem details
     */
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            org.hibernate.exception.ConstraintViolationException.class
    })
    public ProblemDetail handleDataIntegrityViolation(Exception ex) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problemDetail.setTitle("Conflict violation");
        problemDetail.setDetail("Resource cannot be modified or deleted because it is referenced by other records.");

        return problemDetail;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Invalid Request");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }
}
