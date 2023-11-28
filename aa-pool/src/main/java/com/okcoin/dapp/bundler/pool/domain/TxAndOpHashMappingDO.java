package com.okcoin.dapp.bundler.pool.domain;

import lombok.Data;

import java.util.List;

/**
 * @author Yukino.Xin on 2023/10/28 21:26
 */

@Data
public class TxAndOpHashMappingDO {

    private String txHash;

    private List<String> opHashList;

    public TxAndOpHashMappingDO(String txHash, List<String> opHashList) {
        this.txHash = txHash;
        this.opHashList = opHashList;
    }
}
