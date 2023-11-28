package com.okcoin.dapp.bundler.infra.chain.web3j.resp;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TransactionReceiptCommon extends TransactionReceipt {

    @JSONField(serialize = false)
    private String rawResponse;

    public BigInteger resolveEffectiveGasPrice() {
        return Numeric.decodeQuantity(getEffectiveGasPrice());
    }
}
