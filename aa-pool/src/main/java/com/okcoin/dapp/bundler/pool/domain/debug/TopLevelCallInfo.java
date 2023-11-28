package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.Map;

@Data
public class TopLevelCallInfo {

    private String topLevelMethodSig;

    private String topLevelTargetAddress;

    private Map<String, Integer> opcodes;

    private Map<String, AccessInfo> access;

    private Map<String, ContractSizeInfo> contractSize;

    private Map<String, String> extCodeAccessInfo;

    private boolean oog;
}
