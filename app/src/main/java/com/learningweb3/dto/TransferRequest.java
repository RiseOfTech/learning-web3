package com.learningweb3.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "Recipient address must not be blank")
        @Pattern(
                regexp  = "^0x[0-9a-fA-F]{40}$",
                message = "Recipient address must be a valid Ethereum address (0x + 40 hex chars)"
        )
        String to,

        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than zero")
        BigDecimal amount

) {}
