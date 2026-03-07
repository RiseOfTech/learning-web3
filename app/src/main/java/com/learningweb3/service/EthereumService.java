package com.learningweb3.service;

import com.learningweb3.dto.TransferRequest;
import com.learningweb3.dto.TransferResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Business-logic interface for Ethereum operations exposed to the REST layer.
 *
 * <p>The service layer is responsible for:
 * <ul>
 *   <li>Orchestrating calls to {@link com.learningweb3.client.EthereumClient}.</li>
 *   <li>Converting domain exceptions into appropriate application exceptions.</li>
 *   <li>Mapping raw Web3j types to clean DTO objects.</li>
 * </ul>
 *
 * <p>Methods return Project Reactor {@link Mono} types so they integrate naturally
 * with the WebFlux stack and allow non-blocking composition.
 */
public interface EthereumService {

    /**
     * Retrieves the ETH balance for the given Ethereum address.
     *
     * @param address 0x-prefixed Ethereum address.
     * @return {@link Mono} emitting the balance in ETH, or an error signal on failure.
     */
    Mono<BigDecimal> getBalance(String address);

    /**
     * Sends ETH from the application's configured wallet to the specified recipient.
     *
     * @param request validated transfer parameters.
     * @return {@link Mono} emitting the transfer result once mined.
     */
    Mono<TransferResponse> sendTransfer(TransferRequest request);
}
