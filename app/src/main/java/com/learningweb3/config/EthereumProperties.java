package com.learningweb3.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Strongly-typed binding of the {@code ethereum.*} section in {@code application.yml}.
 *
 * <p>Spring Boot reads environment variables automatically when the YAML value is
 * expressed as {@code ${ENV_VAR}}, so there is no explicit env-var reading code here —
 * the framework handles the injection.  If a required variable is absent the
 * application will fail to start with a clear message.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ethereum")
public class EthereumProperties {

    /** Ethereum JSON-RPC endpoint.  Injected from {@code WEB3J_RPC_URL}. */
    @NotBlank(message = "ethereum.rpc-url must not be blank — set WEB3J_RPC_URL")
    private String rpcUrl;

    /** Raw hex private key of the signing wallet.  Injected from {@code WEB3J_PRIVATE_KEY}. */
    @NotBlank(message = "ethereum.private-key must not be blank — set WEB3J_PRIVATE_KEY")
    private String privateKey;

    /** Contract address of the ERC-20 token to monitor for Transfer events. */
    @NotBlank
    private String usdtContractAddress;

    /** How many RPC polls to attempt while waiting for a transaction receipt. */
    @Positive
    private int txReceiptPollAttempts = 40;

    /** Milliseconds to wait between each receipt poll. */
    @Positive
    private long txReceiptPollIntervalMs = 2_000;
}
