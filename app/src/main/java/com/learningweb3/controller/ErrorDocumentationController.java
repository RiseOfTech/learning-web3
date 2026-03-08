package com.learningweb3.controller;

import com.learningweb3.dto.ErrorDocumentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * REST contract for error documentation.
 *
 * <p>Every error type URI used in ProblemDetail responses points to
 * an endpoint defined here — so the type is a real, resolvable URL.
 *
 * <p>Example:
 * <pre>
 *   GET /api/v1/errors/validation-error
 *   GET /api/v1/errors
 * </pre>
 */
public interface ErrorDocumentationController {

    /**
     * Returns documentation for a specific error code.
     *
     * @param errorCode e.g. "validation-error", "ethereum-error"
     * @return 200 with documentation or 404 if the code is unknown
     */
    @GetMapping("/{errorCode}")
    ResponseEntity<ErrorDocumentation> getErrorDoc(@PathVariable String errorCode);

    /**
     * Returns all known error codes and their documentation.
     *
     * @return map of errorCode → documentation
     */
    @GetMapping
    ResponseEntity<Map<String, ErrorDocumentation>> getAllErrors();
}