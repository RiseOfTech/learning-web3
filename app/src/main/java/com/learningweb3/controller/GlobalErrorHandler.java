package com.learningweb3.controller;

import com.learningweb3.exception.EthereumException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 *
 * <p>Using {@link ProblemDetail} (RFC 9457 / formerly RFC 7807)
 * gives callers a machine-readable error body:
 * <pre>
 * {
 *   "type":     "https://learningweb3.dev/errors/ethereum-error",
 *   "title":    "Ethereum Operation Failed",
 *   "status":   502,
 *   "detail":   "eth_getBalance failed: execution reverted",
 *   "instance": "/api/v1/ethereum/balance/0xBAD"
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final String ERROR_TYPE_BASE = "https://learningweb3.dev/errors/";

    // ── Domain errors ─────────────────────────────────────────────────────

    /**
     * Maps {@link EthereumException} to HTTP 502 Bad Gateway.
     *
     * <p>502 is appropriate because the failure originates from an upstream
     * dependency (the Ethereum node), not from a bad client request.
     */
    @ExceptionHandler(EthereumException.class)
    public ResponseEntity<ProblemDetail> handleEthereumException(EthereumException ex) {
        log.error("Ethereum operation failed: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setType(URI.create(ERROR_TYPE_BASE + "ethereum-error"));
        problem.setTitle("Ethereum Operation Failed");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    // ── Validation errors ─────────────────────────────────────────────────

    /**
     * Maps {@code @Valid} / bean-validation failures to HTTP 400 Bad Request.
     *
     * <p>The response body includes a {@code violations} map so callers know
     * exactly which fields failed and why, e.g.:
     * <pre>
     *   "violations": {
     *     "to":     "Recipient address must be a valid Ethereum address",
     *     "amount": "Amount must be greater than zero"
     *   }
     * </pre>
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(WebExchangeBindException ex) {
        Map<String, String> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing   // keep first message per field
                ));

        log.warn("Validation failed: {}", violations);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more request fields are invalid");
        problem.setType(URI.create(ERROR_TYPE_BASE + "validation-error"));
        problem.setTitle("Validation Failed");
        problem.setProperty("violations", violations);
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.badRequest().body(problem);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setType(URI.create(ERROR_TYPE_BASE + "internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
