package com.okcoin.dapp.bundler.pool.gasprice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpL1BlockInfo {

    /// @notice The overhead value applied to the L1 portion of the transaction fee.
    private int l1FeeOverhead;

    /// @notice The scalar value applied to the L1 portion of the transaction fee.
    private BigInteger l1FeeScalar;

    /// @notice The latest L1 basefee.
    private BigInteger l1BaseFee;
}
