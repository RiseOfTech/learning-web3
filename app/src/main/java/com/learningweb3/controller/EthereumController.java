package com.learningweb3.controller;

import com.learningweb3.dto.TransferRequest;
import com.learningweb3.dto.TransferResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * REST API contract for Ethereum operations.
 *
 * <p>Endpoints:
 * <pre>
 *   GET  /balance/{address}   →  ETH balance as BigDecimal
 *   POST /transfer            →  TransferResponse with receipt details
 * </pre>
 */
@RequestMapping("/api/v1/ethereum")
public interface EthereumController {

    /**
     * Returns the ETH balance (in Ether) of the given Ethereum address.
     *
     * <p>Example:
     * <pre>
     *   curl http://localhost:8080/api/v1/ethereum/balance/0xAnyAddress
     * </pre>
     *
     * @param address 42-character Ethereum address (0x + 40 hex chars).
     * @return {@code 200 OK} with the ETH balance, or {@code 400/502} on error.
     */
    @GetMapping("/balance/{address}")
    Mono<ResponseEntity<BigDecimal>> getBalance(@PathVariable String address);

    /**
     * Sends ETH from the application's signing wallet to the specified address.
     *
     * <p>The request body must be valid JSON, e.g.:
     * <pre>
     *   curl -X POST http://localhost:8080/api/v1/ethereum/transfer \
     *        -H 'Content-Type: application/json' \
     *        -d '{"to":"0xRecipientAddress","amount":0.001}'
     * </pre>
     *
     * <p>⚠️  This endpoint broadcasts a REAL transaction on whatever network
     * your {@code WEB3J_RPC_URL} points to.  Use a testnet for experiments.
     *
     * @param request validated transfer parameters.
     * @return {@code 200 OK} with the transaction receipt, or {@code 400/502} on error.
     */
    @PostMapping("/transfer")
    Mono<ResponseEntity<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request);
}
