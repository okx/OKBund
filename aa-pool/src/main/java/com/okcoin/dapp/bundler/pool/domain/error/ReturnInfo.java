package com.okcoin.dapp.bundler.pool.domain.error;

import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint48;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class ReturnInfo extends DynamicStruct {

    private final Uint256 preOpGas;

    private final Uint256 prefund;

    private final Bool sigFailed;

    private final Uint48 validAfter;

    private final Uint48 validUntil;

    private final DynamicBytes paymasterContext;

    public ReturnInfo(Uint256 preOpGas, Uint256 prefund, Bool sigFailed, Uint48 validAfter, Uint48 validUntil, DynamicBytes paymasterContext) {
        super(preOpGas, prefund, sigFailed, validAfter, validUntil, paymasterContext);
        this.preOpGas = preOpGas;
        this.prefund = prefund;
        this.sigFailed = sigFailed;
        this.validAfter = validAfter;
        this.validUntil = validUntil;
        this.paymasterContext = paymasterContext;
    }

    public BigInteger getPreOpGas() {
        return preOpGas.getValue();
    }

    public BigInteger getPrefund() {
        return prefund.getValue();
    }

    public boolean isSigFailed() {
        return sigFailed.getValue();
    }

    public long getValidAfter() {
        return validAfter.getValue().longValue();
    }

    public long getValidUntil() {
        long validUntil = this.validUntil.getValue().longValue();
        if (validUntil == 0) {
            return Web3Constant.UINT48_MAX;
        }
        return validUntil;
    }

    public String getPaymasterContext() {
        return Numeric.toHexString(paymasterContext.getValue());
    }
}
