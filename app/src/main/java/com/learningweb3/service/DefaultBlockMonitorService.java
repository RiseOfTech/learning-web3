package com.learningweb3.service;

import com.learningweb3.client.EthereumClient;
import com.learningweb3.config.EthereumProperties;
import com.learningweb3.constants.EthereumConstants;
import io.reactivex.disposables.CompositeDisposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

/**
 * Subscribes to Ethereum on-chain events using Web3j's RxJava Flowables
 * and logs them via SLF4J.
 *
 * <h2>RxJava → Project Reactor bridge</h2>
 * <p>Web3j uses RxJava 2 for its reactive streams.  Rather than converting to
 * Reactor (which adds complexity), we subscribe directly on RxJava's
 * {@link io.reactivex.Scheduler} and use {@link CompositeDisposable} to manage
 * lifecycle.  In a more complex application you might bridge with
 * {@code reactor.adapter.rxjava.RxJava2Adapter} to compose with Reactor operators.
 *
 * <h2>Back-pressure note</h2>
 * <p>Web3j's HTTP-based Flowables use {@link io.reactivex.BackpressureStrategy#BUFFER}.
 * During high-throughput periods the buffer could grow unbounded.  A production
 * system would apply {@code onBackpressureDrop()} or switch to a WebSocket endpoint
 * which offers natural push-based flow control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultBlockMonitorService implements BlockMonitorService {

    private final EthereumClient      ethereumClient;
    private final EthereumProperties  props;

    /**
     * Holds all active RxJava subscriptions.  Disposing this single object
     * cancels all of them at once — a clean, leak-free shutdown pattern.
     */
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    @PostConstruct
    public void startMonitoring() {
        log.info("Starting on-chain event monitoring...");
        subscribeToBlocks();
        subscribeToErc20Transfers();
    }

    @Override
    @PreDestroy
    public void stopMonitoring() {
        log.info("Stopping on-chain event monitoring, disposing {} subscriptions",
                disposables.size());
        disposables.dispose();
    }

    /**
     * Subscribes to every new Ethereum block and logs summary information.
     *
     * <p>Ethereum mainnet produces a new block roughly every 12 seconds.
     * Each log line shows: block number, timestamp (UTC), and transaction count.
     */
    private void subscribeToBlocks() {
        var disposable = ethereumClient
                .subscribeToNewBlocks(false)   // false = compact block (no full tx data)
                .subscribe(
                        block -> {
                            // Block timestamps are Unix seconds — convert for readability.
                            Instant blockTime = Instant.ofEpochSecond(
                                    block.getTimestamp().longValue());
                            int txCount = block.getTransactions().size();

                            log.info("[BLOCK] #{} | {} | {} transactions",
                                    block.getNumber(), blockTime, txCount);
                        },
                        error -> log.error("[BLOCK] Subscription error: {}", error.getMessage(), error)
                );

        disposables.add(disposable);
        log.info("Block subscription started");
    }

    /**
     * Subscribes to ERC-20 Transfer events emitted by the configured USDT contract.
     *
     * <h3>How ERC-20 Transfer events work</h3>
     * <p>Every ERC-20 transfer emits a log entry with:
     * <pre>
     *   topic[0] = keccak256("Transfer(address,address,uint256)")  ← event signature
     *   topic[1] = from address (ABI-encoded, 32 bytes, left-padded)
     *   topic[2] = to   address (ABI-encoded, 32 bytes, left-padded)
     *   data     = amount (ABI-encoded uint256, 32 bytes)
     * </pre>
     *
     * <p>We manually decode the topics since we're not using a generated contract
     * wrapper.  This is intentional — it shows the raw encoding so learners
     * understand what's actually on-chain.
     */
    private void subscribeToErc20Transfers() {
        String contractAddress = props.getUsdtContractAddress();

        var disposable = ethereumClient
                .subscribeToErc20Transfers(contractAddress)
                .subscribe(
                        this::logTransferEvent,
                        error -> log.error("[ERC20] Subscription error: {}", error.getMessage(), error)
                );

        disposables.add(disposable);
        log.info("ERC-20 Transfer subscription started for contract: {}", contractAddress);
    }

    /**
     * Decodes and logs a single ERC-20 Transfer event log entry.
     */
    private void logTransferEvent(Log eventLog) {
        try {
            List<String> topics = eventLog.getTopics();

            // Guard: a valid Transfer log has exactly 3 topics.
            if (topics == null || topics.size() < 3) {
                log.warn("[ERC20] Unexpected topic count in log: {}", eventLog.getTransactionHash());
                return;
            }

            // topic[1] and topic[2] are 32-byte ABI words with the address in the last 20 bytes.
            String from   = decodeAddressFromTopic(topics.get(1));
            String to     = decodeAddressFromTopic(topics.get(2));

            // data contains the transfer amount as a 32-byte big-endian uint256.
            BigInteger rawAmount = decodeUint256(eventLog.getData());

            // USDT uses 6 decimals (not the standard 18).
            BigDecimal humanAmount = new BigDecimal(rawAmount)
                    .divide(EthereumConstants.USDT_DECIMALS_DIVISOR);

            log.info("[ERC20 Transfer] USDT | from: {} → to: {} | amount: {} USDT | tx: {}",
                    from, to, humanAmount, eventLog.getTransactionHash());

        } catch (Exception ex) {
            log.warn("[ERC20] Failed to decode Transfer event: {}", ex.getMessage());
        }
    }

    /**
     * Extracts an Ethereum address (20 bytes) from a 32-byte ABI-encoded topic.
     *
     * <p>ABI encoding pads addresses on the left with 12 zero bytes.
     * E.g.: {@code 0x000000000000000000000000dac17f958d2ee523a2206206994597c13d831ec7}
     * The address starts at character position 26 (0x + 24 padding chars).
     */
    private static String decodeAddressFromTopic(String topic) {
        // Remove "0x" prefix, then take the last 40 hex characters (20 bytes).
        String hex = topic.startsWith("0x") ? topic.substring(2) : topic;
        return "0x" + hex.substring(hex.length() - EthereumConstants.ETH_ADDRESS_HEX_LENGTH);
    }

    /**
     * Decodes a 32-byte big-endian hex string into a {@link BigInteger}.
     *
     * <p>The {@code data} field of a log is a concatenation of ABI-encoded values.
     * For a single uint256, it is exactly 32 bytes (64 hex chars + "0x" prefix).
     */
    private static BigInteger decodeUint256(String data) {
        String hex = data.startsWith("0x") ? data.substring(2) : data;
        if (hex.isEmpty()) {
            return BigInteger.ZERO;
        }
        return new BigInteger(hex, 16);
    }
}
