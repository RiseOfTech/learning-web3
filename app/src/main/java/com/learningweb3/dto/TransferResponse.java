package com.learningweb3.dto;

import java.math.BigInteger;

public record TransferResponse(
        String     transactionHash,
        BigInteger blockNumber,
        String     from,
        String     to,
        String     status,
        BigInteger gasUsed
) {}
