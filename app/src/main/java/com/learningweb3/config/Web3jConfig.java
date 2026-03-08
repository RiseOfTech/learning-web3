package com.learningweb3.config;

import com.learningweb3.exception.EthereumException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * Spring configuration that wires up the Web3j infrastructure beans.
 *
 * <p>Three beans are created here:
 * <ol>
 *   <li>{@link Web3j} — the main client used for all JSON-RPC calls.</li>
 *   <li>{@link Credentials} — the signing identity loaded from the private key.</li>
 *   <li>{@link ContractGasProvider} — default gas strategy (good for demos; use
 *       a dynamic provider in production).</li>
 * </ol>
 *
 * <p>All sensitive values come from {@link EthereumProperties}, which in turn reads
 * them from environment variables ({@code WEB3J_RPC_URL}, {@code WEB3J_PRIVATE_KEY}).
 * No secret ever appears in source code.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EthereumProperties.class)
public class Web3jConfig {

    private final EthereumProperties props;

    /**
     * Creates the {@link Web3j} instance connected to the configured RPC endpoint.
     *
     * <p>Web3j's {@link HttpService} opens an OkHttp connection pool internally, so
     * this bean should be a singleton (Spring's default) — do NOT create a new
     * {@code Web3j} per request.
     */
    @Bean
    public Web3j web3j() {
        log.info("Connecting Web3j to RPC endpoint: {}", maskUrl(props.getRpcUrl()));
        return Web3j.build(new HttpService(props.getRpcUrl()));
    }

    /**
     * Loads the Ethereum {@link Credentials} from the raw hex private key.
     *
     * <p>Web3j derives the public key and address automatically from the private key,
     * so you never have to supply an address separately.
     *
     * @throws EthereumException if the private key is malformed.
     */
    @Bean
    public Credentials credentials() {
        try {
            Credentials credentials = Credentials.create(props.getPrivateKey());
            log.info("Loaded credentials for address: {}", credentials.getAddress());
            return credentials;
        } catch (Exception ex) {
            throw new EthereumException(
                    "Failed to load credentials — check WEB3J_PRIVATE_KEY format", ex);
        }
    }

    /**
     * Provides gas price / gas limit for contract interactions.
     *
     * <p>{@link DefaultGasProvider} uses hard-coded values suitable for development.
     * For production, replace with a gas station API or EIP-1559 provider.
     */
    @Bean
    public ContractGasProvider gasProvider() {
        return new DefaultGasProvider();
    }

    /** Masks an Infura/Alchemy API key in log output (everything after the last '/'). */
    private static String maskUrl(String url) {
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == url.length() - 1) {
            return url;
        }
        return url.substring(0, lastSlash + 1) + "***";
    }
}
