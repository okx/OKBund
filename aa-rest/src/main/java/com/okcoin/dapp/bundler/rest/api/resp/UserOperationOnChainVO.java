package com.okcoin.dapp.bundler.rest.api.resp;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserOperationOnChainVO {
    private UserOperationVO userOperation;
    private String entryPoint;
    private String transactionHash;
    private String blockHash;
    private String blockNumber;

}
