package com.learningweb3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the learning-web3 Spring Boot application.
 *
 * <p>This project demonstrates how a traditional Java/Spring backend developer
 * can integrate with the Ethereum blockchain using Web3j:
 * <ul>
 *   <li>Read ETH balances (read-only RPC call)</li>
 *   <li>Send ETH transfers (signed transaction)</li>
 *   <li>Subscribe to new blocks (reactive stream)</li>
 *   <li>Monitor ERC-20 Transfer events (reactive stream)</li>
 * </ul>
 */
@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
}
