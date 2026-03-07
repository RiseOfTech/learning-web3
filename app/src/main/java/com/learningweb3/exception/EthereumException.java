package com.learningweb3.exception;

/**
 * Domain exception thrown when an Ethereum operation fails.
 *
 * <p>This is a {@link RuntimeException} so callers are not forced to declare it,
 * but the global error handler ({@link com.learningweb3.controller.GlobalErrorHandler})
 * catches it and maps it to an appropriate HTTP response.
 *
 * <p>Example usages:
 * <ul>
 *   <li>RPC connection failure</li>
 *   <li>Invalid Ethereum address format</li>
 *   <li>Transaction reverted on-chain</li>
 *   <li>Insufficient ETH balance for transfer + gas</li>
 * </ul>
 */
public class EthereumException extends RuntimeException {

    public EthereumException(String message) {
        super(message);
    }

    public EthereumException(String message, Throwable cause) {
        super(message, cause);
    }
}
