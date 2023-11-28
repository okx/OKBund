package com.okcoin.dapp.bundler.infra.storage.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UopAndTxMapEntity implements Serializable {

    /**
     * user opHash
     */
    private String opHash;


    /**
     * tx hash
     */
    private String txHash;


}
