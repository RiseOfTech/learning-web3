package com.learningweb3.client;

import com.learningweb3.config.EthereumProperties;
import com.learningweb3.constants.EthereumConstants;
import com.learningweb3.exception.EthereumException;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Web3j-backed implementation of {@link EthereumClient}.
 *
 * <p>This class is the only place in the codebase that imports Web3j classes,
 * keeping the Ethereum dependency isolated and testable.
 *
 * <h2>Key concepts shown here</h2>
 * <ul>
 *   <li><strong>JSON-RPC over HTTP</strong> — {@code web3j.ethGetBalance().send()} makes
 *       a synchronous {@code eth_getBalance} call to the node.</li>
 *   <li><strong>Signed transactions</strong> — {@link Transfer#sendFunds} builds a raw
 *       transaction, signs it with {@link Credentials}, and broadcasts it.</li>
 *   <li><strong>Event subscriptions</strong> — {@code web3j.blockFlowable()} and
 *       {@code web3j.ethLogFlowable()} return RxJava 2 {@link Flowable}s backed by
 *       Web3j's polling mechanism (it polls {@code eth_getBlockByNumber} /
 *       {@code eth_getLogs} on a schedule).  WebSocket endpoints get push instead.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Web3jEthereumClient implements EthereumClient {

    private final Web3j              web3j;
    private final Credentials        credentials;
    private final EthereumProperties props;

    // ── Balance ───────────────────────────────────────────────────────────

    @Override
    public BigDecimal getBalance(String address) throws IOException {
        log.debug("Fetching ETH balance for address: {}", address);

        EthGetBalance response = web3j
                .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send();

        if (response.hasError()) {
            throw new EthereumException(
                    "ETH getBalance failed: " + response.getError().getMessage());
        }

        // Convert Wei → Ether.  1 ETH = 10^18 Wei.
        BigDecimal balanceWei   = new BigDecimal(response.getBalance());
        BigDecimal balanceEther = balanceWei.divide(EthereumConstants.WEI_PER_ETHER);

        log.debug("Balance for {}: {} ETH", address, balanceEther);
        return balanceEther;
    }

    // ── Transfer ──────────────────────────────────────────────────────────

    @Override
    public TransactionReceipt sendEther(String toAddress, BigDecimal amountEth)
            throws IOException, InterruptedException {

        log.info("Sending {} ETH to {} from {}",
                amountEth, toAddress, credentials.getAddress());

        /*
         * Transfer.sendFunds() does several things under the hood:
         *  1. Calls eth_getTransactionCount to get the sender's nonce.
         *  2. Builds a transaction object with gasPrice, gasLimit, value (in Wei).
         *  3. Signs the transaction with the private key (no network contact needed).
         *  4. Calls eth_sendRawTransaction to broadcast it.
         *  5. Polls eth_getTransactionReceipt until the tx is mined.
         *
         * In production you would replace Transfer with a custom transaction
         * builder to support EIP-1559 (maxFeePerGas / maxPriorityFeePerGas).
         */
        try {
            TransactionReceipt receipt = Transfer.sendFunds(
                    web3j,
                    credentials,
                    toAddress,
                    amountEth,
                    Convert.Unit.ETHER   // tells Web3j the amount is in Ether, not Wei
            ).send();

            log.info("Transfer mined — txHash: {}, block: {}, status: {}",
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber(),
                    receipt.getStatus());

            return receipt;

        } catch (EthereumException ex) {
            throw ex; // re-throw our own type unchanged
        } catch (Exception ex) {
            throw new EthereumException("Failed to send ETH transfer: " + ex.getMessage(), ex);
        }
    }

    // ── Block subscription ────────────────────────────────────────────────

    @Override
    public Flowable<EthBlock.Block> subscribeToNewBlocks(boolean fullTransactionObjects) {
        log.info("Subscribing to new blocks (fullTx={})", fullTransactionObjects);
        /*
         * blockFlowable() polls eth_getBlockByNumber on a timer.
         * With a WebSocket URL it uses eth_subscribe instead (real push).
         * The returned Flowable uses a BUFFER back-pressure strategy.
         */
        return web3j.blockFlowable(fullTransactionObjects)
                    .map(EthBlock::getBlock);
    }

    // ── ERC-20 Transfer event subscription ───────────────────────────────

    @Override
    public Flowable<Log> subscribeToErc20Transfers(String contractAddress) {
        log.info("Subscribing to ERC-20 Transfer events on contract: {}", contractAddress);

        /*
         * EthFilter tells Web3j which logs to fetch:
         *  - fromBlock: LATEST → only new logs from this point forward.
         *  - toBlock:   LATEST → always look at the latest block (open-ended).
         *  - address:   the ERC-20 contract we care about.
         *  - topic[0]:  keccak256("Transfer(address,address,uint256)") — the event signature.
         *
         * Web3j polls eth_getLogs on a schedule and emits each matching Log entry.
         */
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        ).addSingleTopic(EthereumConstants.ERC20_TRANSFER_EVENT_SIGNATURE);

        return web3j.ethLogFlowable(filter);
    }

    // ── Utility ───────────────────────────────────────────────────────────

    @Override
    public BigInteger getChainId() throws IOException {
        return web3j.ethChainId().send().getChainId();
    }
}
