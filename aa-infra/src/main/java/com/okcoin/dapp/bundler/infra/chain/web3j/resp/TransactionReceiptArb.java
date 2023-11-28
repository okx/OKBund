package com.okcoin.dapp.bundler.infra.chain.web3j.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransactionReceiptArb extends TransactionReceiptCommon {

    private String gasUsedForL1;

    private String l1BlockNumber;

    public BigInteger resolveGasUsedForL1() {
        return Numeric.decodeQuantity(gasUsedForL1);
    }

    public BigInteger resolveL1BlockNumber() {
        return Numeric.decodeQuantity(l1BlockNumber);
    }

}
