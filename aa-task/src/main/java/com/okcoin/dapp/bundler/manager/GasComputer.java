package com.okcoin.dapp.bundler.manager;

import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.util.MathUtil;
import lombok.Getter;

import java.math.BigInteger;


@Getter
public class GasComputer {

    private BigInteger totalGasLimitForExe = BigInteger.ZERO;
    private BigInteger totalGasLimitForPay = BigInteger.ZERO;
    private BigInteger totalMaxPriorityFeePerGasCost = BigInteger.ZERO;
    private BigInteger totalMaxFeePerGasCost = BigInteger.ZERO;
    private final IChain chain;

    private final BigInteger gasLimitMax;

    public GasComputer(IChain chain, BigInteger gasLimitMax) {
        this.chain = chain;
        this.gasLimitMax = gasLimitMax;
    }

    public void add(UserOperationDO uop) {
        BigInteger gasLimitForExe = uop.resolveGasLimitExe();
        BigInteger gasLimitForPay = uop.getGasLimitForPay();

        // TODO YUKINO 2023/10/31: 需要一个判断逻辑
        totalGasLimitForExe = totalGasLimitForExe.add(gasLimitForExe);
        totalGasLimitForPay = totalGasLimitForPay.add(gasLimitForPay);
        totalMaxPriorityFeePerGasCost = totalMaxPriorityFeePerGasCost.add(uop.getMaxPriorityFeePerGas().multiply(gasLimitForPay));
        totalMaxFeePerGasCost = totalMaxFeePerGasCost.add(uop.getMaxFeePerGas().multiply(gasLimitForPay));
    }

    public TransactionGas toTransactionGas(BigInteger baseFee) {
        BigInteger maxPriorityFeePerGas = totalMaxPriorityFeePerGasCost.divide(totalGasLimitForPay);
        BigInteger maxFeePerGas = totalMaxFeePerGasCost.divide(totalGasLimitForPay);
        if (chain.isEip1559()) {
            maxFeePerGas = MathUtil.min(maxFeePerGas, baseFee.add(maxPriorityFeePerGas));
        } else {
            maxFeePerGas = maxPriorityFeePerGas;
        }
        return new TransactionGas(totalGasLimitForExe, maxFeePerGas, maxPriorityFeePerGas);
    }
}
