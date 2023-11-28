package com.okcoin.dapp.bundler.pool.gasprice;

import com.okcoin.dapp.bundler.pool.util.MathUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
public class GasPriceInfo {

    private BigInteger baseFee;

    private BigInteger maxPriorityFeePerGas;

    private long timestamp;

    public BigInteger resolveMaxFeePerGas() {
        // TODO YUKINO 2023/10/26: 优化price
        return MathUtil.multiply(baseFee, BigDecimal.valueOf(1.5)).add(maxPriorityFeePerGas);
    }

    public GasPriceInfo(BigInteger baseFee, BigInteger maxPriorityFeePerGas, long timestamp) {
        this.baseFee = baseFee;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.timestamp = timestamp;
    }
}
