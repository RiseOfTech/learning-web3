package com.learningweb3.controller;

import com.learningweb3.dto.ErrorDocumentation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

public interface ErrorDocumentationController {

    @GetMapping("/{errorCode}")
    ResponseEntity<ErrorDocumentation> getErrorDoc(@PathVariable String errorCode);

    @GetMapping
    ResponseEntity<Map<String, ErrorDocumentation>> getAllErrors();
}