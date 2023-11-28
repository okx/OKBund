package com.okcoin.dapp.bundler.pool.gasprice;

import com.google.common.collect.Maps;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.TransactionUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.ChainIdConstant;
import com.okcoin.dapp.bundler.pool.config.GasConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class GasService {

    private final Map<Long, GasPriceInfo> gasClientPriceInfoMap = Maps.newConcurrentMap();

    private final Map<Long, L1GasInfo> l1GasInfoMap = Maps.newConcurrentMap();

    @Autowired
    private ArbGasInfoContract arbGasInfoContract;

    @Autowired
    private OpL1BlockContract opL1BlockContract;

    @Autowired
    private GasConfig gasConfig;

    public GasPriceInfo getGasPriceInfoWithCache(IChain chain) {
        Map<Long, GasPriceInfo> gasPriceInfoMap = gasClientPriceInfoMap;
        GasPriceInfo gasPriceInfo = gasPriceInfoMap.get(chain.getChainId());
        long current = System.currentTimeMillis();

        if (gasPriceInfo != null && gasPriceInfo.getTimestamp() + TimeUnit.SECONDS.toMillis(gasConfig.getGasPriceCacheTimeSecond()) > current) {
            return gasPriceInfo;
        }

        gasPriceInfo = getGasPriceInfo(chain);
        gasPriceInfoMap.put(chain.getChainId(), gasPriceInfo);

        return gasPriceInfo;

    }

    public L1GasInfo getL1GasInfoWithCache(IChain chain) {
        L1GasInfo l1GasInfo = l1GasInfoMap.get(chain.getChainId());
        long current = System.currentTimeMillis();

        if (l1GasInfo != null && l1GasInfo.getTimestamp() + TimeUnit.SECONDS.toMillis(gasConfig.getGasPriceCacheTimeSecond()) > current) {
            return l1GasInfo;
        }

        l1GasInfo = getL1GasInfo(chain);
        l1GasInfoMap.put(chain.getChainId(), l1GasInfo);

        return l1GasInfo;

    }


    private GasPriceInfo getGasPriceInfo(IChain chain) {
        long timestamp = System.currentTimeMillis();
        BigInteger maxPriorityFeePerGas;
        BigInteger baseFee;
        if (chain.isEip1559()) {
            TransactionUtil.ChainFee chainFee = TransactionUtil.get1559GasPrice(chain, gasConfig.getRewardPercentile());
            maxPriorityFeePerGas = chainFee.getMaxPriorityFeePerGas();
            baseFee = chainFee.getBaseFee();
        } else {
            maxPriorityFeePerGas = TransactionUtil.getGasPrice(chain);
            baseFee = BigInteger.ZERO;
        }

        return new GasPriceInfo(baseFee, maxPriorityFeePerGas, timestamp);
    }

    private L1GasInfo getL1GasInfo(IChain chain) {
        L1GasInfo l1GasInfo;
        long timestamp = System.currentTimeMillis();
        if (chain.getChainId() == ChainIdConstant.ARB_MAIN) {
            BigInteger l1BaseFee = arbGasInfoContract.getL1BaseFeeEstimate(chain);
            l1GasInfo = new L1GasInfo(l1BaseFee, timestamp);
        } else {
            OpL1BlockInfo opL1BlockInfo = opL1BlockContract.getOpL1BlockInfo(chain);
            l1GasInfo = new L1GasInfo(opL1BlockInfo.getL1BaseFee(), timestamp);
            l1GasInfo.setL1FeeOverhead(opL1BlockInfo.getL1FeeOverhead());
            l1GasInfo.setL1FeeScalar(opL1BlockInfo.getL1FeeScalar());
        }

        return l1GasInfo;
    }
}
