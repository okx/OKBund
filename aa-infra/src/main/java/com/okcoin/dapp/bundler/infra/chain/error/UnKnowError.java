package com.okcoin.dapp.bundler.infra.chain.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnKnowError implements IEvmError {

    private final String message;

    private final ChainErrorMsg error;

    public UnKnowError(ChainErrorMsg error) {
        this.message = error.getMessage() + (error.getData() == null ? "" : "::" + error.getData());
        this.error = error;
    }
}
