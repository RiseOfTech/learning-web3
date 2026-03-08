package com.learningweb3.controller;

import com.learningweb3.dto.TransferRequest;
import com.learningweb3.dto.TransferResponse;
import com.learningweb3.service.EthereumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DefaultEthereumController implements EthereumController {

    private final EthereumService ethereumService;

    @Override
    public Mono<ResponseEntity<BigDecimal>> getBalance(String address) {
        log.debug("GET /balance/{}", address);
        return ethereumService.getBalance(address)
                              .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<TransferResponse>> transfer(@Valid TransferRequest request) {
        log.debug("POST /transfer to={} amount={}", request.to(), request.amount());
        return ethereumService.sendTransfer(request)
                              .map(ResponseEntity::ok);
    }
}
