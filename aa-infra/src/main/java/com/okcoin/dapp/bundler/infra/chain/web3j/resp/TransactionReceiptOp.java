package com.okcoin.dapp.bundler.infra.chain.web3j.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransactionReceiptOp extends TransactionReceiptCommon {

    private String l1Fee;

    private String l1FeeScalar;

    private String l1GasPrice;

    private String l1GasUsed;

    public BigInteger resolveL1Fee() {
        return Numeric.decodeQuantity(l1Fee);
    }

    public BigInteger resolveL1FeeScalar() {
        return Numeric.decodeQuantity(l1FeeScalar);
    }

    public BigInteger resolveL1GasPrice() {
        return Numeric.decodeQuantity(l1GasPrice);
    }

    public BigInteger resolveL1GasUsed() {
        return Numeric.decodeQuantity(l1GasUsed);
    }

}
