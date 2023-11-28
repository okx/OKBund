package com.okcoin.dapp.bundler.pool.gasprice;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class L1GasInfo {

    private static final BigInteger DECIMAL = BigInteger.valueOf(1000_000);

    private BigInteger l1BaseFee;

    private long timestamp;

    /// @notice The overhead value applied to the L1 portion of the transaction fee.
    private int l1FeeOverhead;

    /// @notice The scalar value applied to the L1 portion of the transaction fee.
    private BigInteger l1FeeScalar;

    public L1GasInfo(BigInteger l1BaseFee, long timestamp) {
        this.l1BaseFee = l1BaseFee;
        this.timestamp = timestamp;
    }

    public BigInteger resolveDecimal() {
        return DECIMAL;
    }
}
