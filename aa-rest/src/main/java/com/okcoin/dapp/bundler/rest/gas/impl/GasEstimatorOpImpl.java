package com.okcoin.dapp.bundler.rest.gas.impl;


import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.constant.ChainIdConstant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.pool.gasprice.L1GasInfo;
import com.okcoin.dapp.bundler.rest.constant.GasConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;


@Service
public class GasEstimatorOpImpl extends GasEstimatorDefaultImpl {

    private static final int SIGNATURE_LENGTH = 68;

    // 12125040168863184969
    private static final BigInteger RANDOM_NONCE = Numeric.toBigInt(CodecUtil.keccak256("Nonce".getBytes()), 0, 4);

    // 968523046
    private static final BigInteger RANDOM_GAS_TIP_CAP = Numeric.toBigInt(CodecUtil.keccak256("GasTipCap".getBytes()), 0, 4);

    // 4054464639
    private static final BigInteger RANDOM_GAS_FEE_CAP = Numeric.toBigInt(CodecUtil.keccak256("GasFeeCap".getBytes()), 0, 4);

    // 3091729570
    private static final BigInteger RANDOM_GAS = Numeric.toBigInt(CodecUtil.keccak256("Gas".getBytes()), 0, 4);

    @Autowired
    private GasService gasService;

    @Override
    public boolean fit(IChain chain) {
        return ChainIdConstant.OP_MAIN == chain.getChainId();
    }

    @Override
    public BigInteger estimatePreVerificationGasForL1(UserOperationDO uop) {
        fillGasForEstimatePreVerificationGas(uop);
        IChain chain = uop.getChain();
        String callData = Entrypoint.getHandleOpsCallData(Lists.newArrayList(uop));

        BigInteger l1GasFee = estimateL1GasFeeFromLocal(uop.getEntryPoint(), callData, chain);
        BigInteger l2BaseFee = gasService.getGasPriceInfoWithCache(chain).getBaseFee();

        return l1GasFee.divide(l2BaseFee);
    }

    public BigInteger estimateL1GasFeeFromLocal(String to, String data, IChain chain) {
        RawTransaction rawTransaction = RawTransaction.createTransaction(chain.getChainId(), RANDOM_NONCE, RANDOM_GAS, to, BigInteger.ZERO, data, RANDOM_GAS_TIP_CAP, RANDOM_GAS_FEE_CAP);
        byte[] rlpEncode = TransactionEncoder.encode(rawTransaction);

        long total = 0;
        for (byte b : rlpEncode) {
            if (b == 0) {
                total += GasConstant.ZERO_BYTE;
            } else {
                total += GasConstant.NON_ZERO_BYTE;
            }
        }

        L1GasInfo l1GasInfo = gasService.getL1GasInfoWithCache(chain);
        BigInteger l1BaseFee = l1GasInfo.getL1BaseFee();
        long unsigned = total + l1GasInfo.getL1FeeOverhead() + (SIGNATURE_LENGTH * GasConstant.NON_ZERO_BYTE);
        return l1BaseFee.multiply(BigInteger.valueOf(unsigned)).multiply(l1GasInfo.getL1FeeScalar()).divide(l1GasInfo.resolveDecimal());
    }

}
