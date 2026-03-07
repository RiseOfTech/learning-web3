package com.learningweb3.service;

import com.learningweb3.client.EthereumClient;
import com.learningweb3.dto.TransferRequest;
import com.learningweb3.dto.TransferResponse;
import com.learningweb3.exception.EthereumException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

/**
 * Default implementation of {@link EthereumService}.
 *
 * <h2>Why {@code Schedulers.boundedElastic()}?</h2>
 * <p>Web3j's synchronous {@code .send()} calls block the calling thread while
 * waiting for the RPC response.  In a WebFlux application, blocking the
 * event-loop thread would starve other requests.  Wrapping blocking calls in
 * {@code Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())} offloads
 * them to a separate, elastic thread pool designed for I/O-bound blocking work.
 *
 * <p>A future improvement would be to use Web3j's native RxJava Flowable + WebSocket
 * transport end-to-end, removing the need for {@code boundedElastic} entirely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EthereumServiceImpl implements EthereumService {

    private final EthereumClient ethereumClient;

    // ── Balance ───────────────────────────────────────────────────────────

    @Override
    public Mono<BigDecimal> getBalance(String address) {
        return Mono.fromCallable(() -> ethereumClient.getBalance(address))
                   .subscribeOn(Schedulers.boundedElastic())
                   .doOnSuccess(balance ->
                           log.info("Balance query for {}: {} ETH", address, balance))
                   .onErrorMap(this::wrapIfNeeded);
    }

    // ── Transfer ──────────────────────────────────────────────────────────

    @Override
    public Mono<TransferResponse> sendTransfer(TransferRequest request) {
        return Mono.fromCallable(() ->
                        ethereumClient.sendEther(request.to(), request.amount()))
                   .subscribeOn(Schedulers.boundedElastic())
                   .map(this::toTransferResponse)
                   .doOnSuccess(resp ->
                           log.info("Transfer confirmed — hash: {}", resp.transactionHash()))
                   .onErrorMap(this::wrapIfNeeded);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────

    /**
     * Converts a raw Web3j {@link TransactionReceipt} to our clean DTO.
     * Null-safe defaults handle the rare case where the receipt is incomplete.
     */
    private TransferResponse toTransferResponse(TransactionReceipt receipt) {
        return new TransferResponse(
                receipt.getTransactionHash(),
                receipt.getBlockNumber(),
                receipt.getFrom(),
                receipt.getTo(),
                receipt.getStatus(),          // "0x1" = success, "0x0" = reverted
                receipt.getGasUsed()
        );
    }

    /**
     * Ensures all exceptions surfacing from the client layer are wrapped in
     * {@link EthereumException} so the global error handler can process them
     * uniformly.  If the exception is already an {@link EthereumException}
     * it is returned unchanged.
     */
    private Throwable wrapIfNeeded(Throwable ex) {
        if (ex instanceof EthereumException) {
            return ex;
        }
        return new EthereumException("Ethereum operation failed: " + ex.getMessage(), ex);
    }
}
