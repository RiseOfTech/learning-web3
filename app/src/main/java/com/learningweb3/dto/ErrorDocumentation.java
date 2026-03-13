package com.learningweb3.dto;

import java.util.List;

public record ErrorDocumentation(
        String errorCode,
        String title,
        String description,
        List<String> possibleCauses,
        String howToFix,
        int httpStatus
) {}
