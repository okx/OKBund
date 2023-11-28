package com.okcoin.dapp.bundler.rest.gas.impl;


import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.constant.ChainIdConstant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.rest.constant.GasConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;

import java.math.BigInteger;


@Service
public class GasEstimatorArbImpl extends GasEstimatorDefaultImpl {

    // 12125040168863184969
    private static final BigInteger RANDOM_NONCE = Numeric.toBigInt(CodecUtil.keccak256("Nonce".getBytes()), 0, 8);

    // 968523046
    private static final BigInteger RANDOM_GAS_TIP_CAP = Numeric.toBigInt(CodecUtil.keccak256("GasTipCap".getBytes()), 0, 4);

    // 4054464639
    private static final BigInteger RANDOM_GAS_FEE_CAP = Numeric.toBigInt(CodecUtil.keccak256("GasFeeCap".getBytes()), 0, 4);

    // 3091729570
    private static final BigInteger RANDOM_GAS = Numeric.toBigInt(CodecUtil.keccak256("Gas".getBytes()), 0, 4);

    private static final byte[] RAND_V = Numeric.toBytesPadded(BigInteger.valueOf(ChainIdConstant.ARB_MAIN * 3 + 35), 3);
    private static final byte[] RAND_R = CodecUtil.keccak256("R".getBytes());
    private static final byte[] RAND_S = CodecUtil.keccak256("S".getBytes());
    private static final Sign.SignatureData RAND_SIGNATURE = new Sign.SignatureData(RAND_V, RAND_R, RAND_S);

    private static final BigInteger ONE_IN_BIPS = BigInteger.valueOf(10000);
    private static final BigInteger GAS_ESTIMATION_L1_PRICE_PADDING = BigInteger.valueOf(11000);
    private static final int ESTIMATION_PADDING_UNITS = 16 * GasConstant.NON_ZERO_BYTE;
    private static final BigInteger ESTIMATION_PADDING_BASIS_POINTS = BigInteger.valueOf(10100);

    @Autowired
    private GasService gasService;

    @Override
    public boolean fit(IChain chain) {
        return ChainIdConstant.ARB_MAIN == chain.getChainId();
    }

    @Override
    public BigInteger estimatePreVerificationGasForL1(UserOperationDO uop) {
        fillGasForEstimatePreVerificationGas(uop);
        IChain chain = uop.getChain();
        String callData = Entrypoint.getHandleOpsCallData(Lists.newArrayList(uop));

        BigInteger l1GasLimit = estimateL1GasUsedFromLocal(uop.getEntryPoint(), callData);
        BigInteger l2BaseFee = gasService.getGasPriceInfoWithCache(chain).getBaseFee();
        BigInteger l1BaseFee = gasService.getL1GasInfoWithCache(chain).getL1BaseFee();

        return l1GasLimit.multiply(l1BaseFee).divide(l2BaseFee);
    }

    public BigInteger estimateL1GasUsedFromLocal(String to, String data) {
        RawTransaction rawTransaction = RawTransaction.createTransaction(0, RANDOM_NONCE, RANDOM_GAS, to, BigInteger.ZERO, data, RANDOM_GAS_TIP_CAP, RANDOM_GAS_FEE_CAP);
        byte[] rlpEncode = TransactionEncoder.encode(rawTransaction, RAND_SIGNATURE);
        byte[] compressed = CodecUtil.brotliFastCompress(rlpEncode);
        return BigInteger.valueOf((long) compressed.length * GasConstant.NON_ZERO_BYTE + ESTIMATION_PADDING_UNITS).multiply(ESTIMATION_PADDING_BASIS_POINTS).divide(ONE_IN_BIPS).multiply(GAS_ESTIMATION_L1_PRICE_PADDING).divide(ONE_IN_BIPS);
    }
}
