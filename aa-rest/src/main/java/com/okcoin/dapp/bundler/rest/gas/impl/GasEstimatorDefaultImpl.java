package com.okcoin.dapp.bundler.rest.gas.impl;


import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.TransactionUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.ChainIdConstant;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.infra.chain.exception.ChainException;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.error.SimulateHandleOpResultOKX;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.gasprice.GasPriceInfo;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.pool.simulation.EntryPointSimulationsFactory;
import com.okcoin.dapp.bundler.pool.simulation.IEntryPointSimulations;
import com.okcoin.dapp.bundler.pool.util.MathUtil;
import com.okcoin.dapp.bundler.rest.config.RestConfig;
import com.okcoin.dapp.bundler.rest.constant.GasConstant;
import com.okcoin.dapp.bundler.rest.gas.IGasEstimator;
import com.okcoin.dapp.bundler.rest.gas.UserOperationGasDO;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static com.okcoin.dapp.bundler.rest.constant.GasConstant.*;


@Service
@Setter
public class GasEstimatorDefaultImpl implements IGasEstimator {

    @Autowired
    private EntryPointSimulationsFactory entryPointSimulationsFactory;

    @Autowired
    private RestConfig restConfig;

    @Autowired
    private GasService gasService;

    @Override
    public boolean fit(IChain chain) {
        return ChainIdConstant.OP_MAIN != chain.getChainId() && ChainIdConstant.ARB_MAIN != chain.getChainId();
    }

    public UserOperationGasDO estimateGasFromEvm(UserOperationDO uop) {
        IChain chain = uop.getChain();
        BigInteger maxFeePerGas = uop.getMaxFeePerGas();
        BigInteger maxPriorityFeePerGas = uop.getMaxPriorityFeePerGas();
        BigInteger preVerificationGas = uop.getPreVerificationGas();
        BigInteger verificationGasLimit = uop.getVerificationGasLimit();
        BigInteger callGasLimit = uop.getCallGasLimit();
        String callData = uop.getCallData();
        String paymasterAndData = uop.getPaymasterAndData();

        uop.setPreVerificationGas(preVerificationGas.compareTo(BigInteger.ZERO) == 0 ? PRE_VERIFICATION_GAS_MIN : preVerificationGas);
        uop.setVerificationGasLimit(verificationGasLimit.compareTo(BigInteger.ZERO) == 0 ? VERIFICATION_GAS_LIMIT_MIN : verificationGasLimit);
        uop.setCallGasLimit(callGasLimit.compareTo(BigInteger.ZERO) == 0 ? CALL_GAS_LIMIT_MIN : callGasLimit);
        if (Web3Constant.HEX_PREFIX.equals(callData)) {
            uop.setCallGasLimit(BigInteger.ZERO);
        }

        GasPriceInfo gasPriceInfo = gasService.getGasPriceInfoWithCache(chain);
        BigInteger maxFeePerGasMin = gasPriceInfo.resolveMaxFeePerGas();
        BigInteger maxPriorityFeePerGasMin = gasPriceInfo.getMaxPriorityFeePerGas();

        uop.setMaxFeePerGas(maxFeePerGas.compareTo(BigInteger.ZERO) == 0 ? maxFeePerGasMin : maxFeePerGas);
        if (chain.isEip1559()) {
            uop.setMaxPriorityFeePerGas(maxPriorityFeePerGas.compareTo(BigInteger.ZERO) == 0 ? maxPriorityFeePerGasMin : maxPriorityFeePerGas);
        } else {
            uop.setMaxPriorityFeePerGas(maxPriorityFeePerGas.compareTo(BigInteger.ZERO) == 0 ? maxFeePerGasMin : maxPriorityFeePerGas);
        }

        IEntryPointSimulations entryPointSimulations = entryPointSimulationsFactory.get(uop.getEntryPoint());
        SimulateHandleOpResultOKX simulateHandleOpResult = entryPointSimulations.simulateHandleOp(uop, true);

        verificationGasLimit = simulateHandleOpResult.getVerificationGasUsed();
        callGasLimit = simulateHandleOpResult.getCallGasUsed();
        preVerificationGas = simulateHandleOpResult.getPreVerificationGas();
        BigInteger postOpGas = simulateHandleOpResult.getPostOpGasUsed();
        long validAfter = simulateHandleOpResult.getValidAfter();
        long validUntil = simulateHandleOpResult.getValidUntil();

        if (Web3Constant.HEX_PREFIX.equals(paymasterAndData)) {
            verificationGasLimit = verificationGasLimit.add(postOpGas);
        }

        if (Web3Constant.HEX_PREFIX.equals(callData)) {
            verificationGasLimit = verificationGasLimit.add(callGasLimit);
            postOpGas = postOpGas.add(callGasLimit);
            callGasLimit = BigInteger.ZERO;
        }

        return new UserOperationGasDO(preVerificationGas, MathUtil.max(verificationGasLimit, postOpGas), callGasLimit, validAfter, validUntil);
    }

    public BigInteger estimateCallGasLimitFromNode(UserOperationDO uop) {
        if (!restConfig.isOpenEstimateGasFromNode() || !Web3Constant.HEX_PREFIX.equals(uop.getInitCode()) || Web3Constant.HEX_PREFIX.equals(uop.getCallData())) {
            return BigInteger.ZERO;
        }
        try {
            return TransactionUtil.estimateGas(uop.getEntryPoint(), uop.getSender(), uop.getCallData(), uop.getChain());
        } catch (ChainException e) {
            throw new AAException(e.getData(), AAExceptionEnum.USER_OPERATION_REVERTED, e.getMsg());
        }
    }

    public BigInteger estimatePreVerificationGas(UserOperationDO uop) {
        fillGasForEstimatePreVerificationGas(uop);
        String callData = Entrypoint.getHandleOpsCallData(Lists.newArrayList(uop));
        byte[] uopData = Numeric.hexStringToByteArray(callData);
        int lengthInWord = (uopData.length + Web3Constant.BYTE_LENGTH - 1) / Web3Constant.BYTE_LENGTH;
        long callDataCost = 0;
        for (byte b : uopData) {
            callDataCost += b == (byte) 0 ? GasConstant.ZERO_BYTE : GasConstant.NON_ZERO_BYTE;
        }

        int perFixedUserOpGas = (restConfig.getFixed() + restConfig.getBundleSize() - 1) / restConfig.getBundleSize();
        return BigInteger.valueOf(callDataCost + perFixedUserOpGas + restConfig.getPerUserOp() + GasConstant.PER_USER_OP_WORD * lengthInWord);
    }

    @Override
    public BigInteger estimatePreVerificationGasForL1(UserOperationDO uop) {
        return BigInteger.ZERO;
    }
}
