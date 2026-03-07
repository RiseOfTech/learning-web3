package com.learningweb3.client;

import io.reactivex.Flowable;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Low-level Ethereum client abstraction.
 *
 * <p>This interface isolates all Web3j calls from the rest of the application.
 * Keeping Web3j behind an interface allows:
 * <ul>
 *   <li>Easy mocking in unit tests (no live node required).</li>
 *   <li>Swapping Web3j for another client library without touching service code.</li>
 *   <li>Adding retry/circuit-breaker logic in a single place.</li>
 * </ul>
 *
 * <p>Methods throw checked {@link IOException} for RPC failures so callers
 * are forced to decide how to handle network errors explicitly.
 */
public interface EthereumClient {

    /**
     * Returns the ETH balance of the given address in <em>Ether</em> (not Wei).
     *
     * @param address 42-character hex address (0x-prefixed).
     * @return balance in ETH with full precision.
     * @throws IOException if the RPC call fails.
     */
    BigDecimal getBalance(String address) throws IOException;

    /**
     * Sends a raw ETH transfer from the configured signing wallet to {@code toAddress}.
     *
     * @param toAddress recipient's 0x-prefixed Ethereum address.
     * @param amountEth ETH to send (converted to Wei internally).
     * @return the on-chain {@link TransactionReceipt} once the tx is mined.
     * @throws IOException          if the RPC call fails.
     * @throws InterruptedException if polling for the receipt is interrupted.
     */
    TransactionReceipt sendEther(String toAddress, BigDecimal amountEth)
            throws IOException, InterruptedException;

    /**
     * Returns a reactive {@link Flowable} that emits a new item for every block
     * mined on the connected chain.  Subscribers receive the full block object.
     *
     * <p>This is a hot, infinite stream; callers are responsible for disposing it
     * (e.g., via {@code dispose()} on the returned {@link io.reactivex.disposables.Disposable}).
     *
     * @param fullTransactionObjects if {@code true}, block objects include full tx data.
     * @return RxJava 2 Flowable of new blocks.
     */
    Flowable<EthBlock.Block> subscribeToNewBlocks(boolean fullTransactionObjects);

    /**
     * Returns a reactive {@link Flowable} that emits raw EVM {@link Log} entries
     * matching the Transfer event of the given ERC-20 contract.
     *
     * @param contractAddress 0x-prefixed address of the ERC-20 token contract.
     * @return RxJava 2 Flowable of matching log entries.
     */
    Flowable<Log> subscribeToErc20Transfers(String contractAddress);

    /**
     * Queries the current chain ID.  Useful for sanity-checking the connected network.
     *
     * @return chain ID (e.g., 1 = mainnet, 11155111 = Sepolia).
     * @throws IOException if the RPC call fails.
     */
    BigInteger getChainId() throws IOException;
}
