package com.learningweb3.dto;

import java.math.BigInteger;

/**
 * Response body returned after a successful ETH transfer.
 *
 * <p>Contains the fields from the on-chain transaction receipt that are most
 * useful for a caller to know about immediately.
 *
 * @param transactionHash The unique identifier of the mined transaction.
 * @param blockNumber     The block in which the transaction was included.
 * @param from            Sender address.
 * @param to              Recipient address.
 * @param status          "1" = success, "0" = reverted (EIP-658 status field).
 * @param gasUsed         Actual gas consumed by the transaction.
 */
public record TransferResponse(
        String     transactionHash,
        BigInteger blockNumber,
        String     from,
        String     to,
        String     status,
        BigInteger gasUsed
) {}
