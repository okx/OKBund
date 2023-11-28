package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.List;

@Data
public class ReferencedCodeHashes {

    private List<String> addresses;

    private String hash;

    public ReferencedCodeHashes(List<String> addresses, String hash) {
        this.addresses = addresses;
        this.hash = hash;
    }
}
