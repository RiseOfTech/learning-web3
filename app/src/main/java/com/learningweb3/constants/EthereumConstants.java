package com.learningweb3.constants;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Application-wide constants for Ethereum calculations.
 *
 * <p>Having a single constants class avoids magic numbers scattered across the code
 * and makes unit conversion immediately obvious to readers unfamiliar with Web3.
 */
public final class EthereumConstants {

    private EthereumConstants() {
        // utility class — do not instantiate
    }

    /**
     * 1 Ether expressed in Wei.
     *
     * <p>All Ethereum values on the wire are in Wei (the smallest unit).
     * <pre>
     *   1 ETH = 10^18 Wei
     * </pre>
     */
    public static final BigDecimal WEI_PER_ETHER = BigDecimal.TEN.pow(18);

    /**
     * Number of decimal places Ether balances are displayed with.
     * (18 decimals = full Wei precision; 8 is a common display precision.)
     */
    public static final int ETHER_DISPLAY_SCALE = 8;

    /**
     * USDT uses 6 decimal places (unlike most ERC-20 tokens which use 18).
     * Divide raw transfer amounts by 10^6 to get the human-readable USDT amount.
     */
    public static final BigDecimal USDT_DECIMALS_DIVISOR = BigDecimal.TEN.pow(6);

    /**
     * Ethereum address length in hex characters (without the "0x" prefix).
     * An address is 20 bytes = 40 hex characters.
     */
    public static final int ETH_ADDRESS_HEX_LENGTH = 40;

    /**
     * The ERC-20 Transfer event signature used to build topic filters.
     *
     * <p>Keccak-256 of the canonical ABI signature:
     * {@code Transfer(address,address,uint256)}
     */
    public static final String ERC20_TRANSFER_EVENT_SIGNATURE =
            "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    /**
     * Number of Wei to pad topics to for indexed address parameters in ABI encoding.
     * (256-bit slots = 32 bytes = 64 hex characters, with the address in the last 40.)
     */
    public static final int ABI_WORD_HEX_LENGTH = 64;

    // ── Gas defaults (informational — actual values come from DefaultGasProvider) ─

    /** Default gas limit for a plain ETH transfer.  21,000 is the minimum. */
    public static final BigInteger GAS_LIMIT_ETH_TRANSFER = BigInteger.valueOf(21_000);
}
