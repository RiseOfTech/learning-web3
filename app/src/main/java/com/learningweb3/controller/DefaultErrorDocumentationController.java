package com.learningweb3.controller;

import com.learningweb3.config.AppProperties;
import com.learningweb3.dto.ErrorDocumentation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
@RequestMapping("${app.errors-path}")
public class DefaultErrorDocumentationController implements ErrorDocumentationController {

    private final AppProperties appProperties;

    private static final Map<String, ErrorDocumentation> ERRORS = Map.of(

            "validation-error", new ErrorDocumentation(
                    "validation-error",
                    "Validation Failed",
                    "One or more request fields failed validation rules",
                    List.of(
                            "Ethereum address does not start with 0x",
                            "Ethereum address is not 42 characters long",
                            "Amount is zero or negative",
                            "Required field is missing"
                    ),
                    "Ensure 'to' is a valid Ethereum address (0x + 40 hex chars) " +
                            "and 'amount' is a positive number",
                    400
            ),

            "ethereum-error", new ErrorDocumentation(
                    "ethereum-error",
                    "Ethereum Operation Failed",
                    "An error occurred while communicating with the Ethereum node " +
                            "or executing a blockchain transaction",
                    List.of(
                            "RPC node is unavailable or rate-limited",
                            "Insufficient ETH balance to cover transfer + gas",
                            "Transaction was reverted on-chain",
                            "Invalid nonce (transaction ordering issue)"
                    ),
                    "Check your ETH balance, verify the RPC endpoint is reachable, " +
                            "and ensure the recipient address is correct",
                    502
            ),

            "internal-error", new ErrorDocumentation(
                    "internal-error",
                    "Internal Server Error",
                    "An unexpected error occurred inside the application",
                    List.of(
                            "Bug in application code",
                            "Unexpected null value",
                            "Misconfiguration"
                    ),
                    "This is not your fault. Please report this error " +
                            "with the timestamp so we can investigate",
                    500
            )
    );

    @Override
    public ResponseEntity<ErrorDocumentation> getErrorDoc(String errorCode) {
        return Optional.ofNullable(ERRORS.get(errorCode))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Map<String, ErrorDocumentation>> getAllErrors() {
        return ResponseEntity.ok(ERRORS);
    }
}