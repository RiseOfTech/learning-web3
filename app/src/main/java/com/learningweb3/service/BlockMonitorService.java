package com.learningweb3.service;

/**
 * Background service that subscribes to on-chain events and logs them.
 *
 * <p>Implementations start their subscriptions on application startup and
 * clean up when the Spring context shuts down.  This is the "reactive monitoring"
 * part of the project — it shows how Web3j's RxJava Flowables can be bridged
 * into Project Reactor for a fully reactive, non-blocking pipeline.
 */
public interface BlockMonitorService {

    /**
     * Starts the background subscriptions:
     * <ol>
     *   <li>New block subscription — logs block number, timestamp, tx count.</li>
     *   <li>ERC-20 Transfer event subscription — logs from/to/amount for each event.</li>
     * </ol>
     *
     * <p>Called automatically by Spring after the application context is ready.
     */
    void startMonitoring();

    /**
     * Disposes all active subscriptions and releases resources.
     *
     * <p>Called automatically by Spring before the application context is closed.
     */
    void stopMonitoring();
}
