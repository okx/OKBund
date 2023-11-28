package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

@Data
public class ContractSizeInfo {

    private String opcode;

    private Integer contractSize;
}
