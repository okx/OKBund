package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScannerResult {

    private List<String> addresses;

    private Map<String, SlotMap> storageMap;

    public ScannerResult(List<String> addresses, Map<String, SlotMap> storageMap) {
        this.addresses = addresses;
        this.storageMap = storageMap;
    }
}
