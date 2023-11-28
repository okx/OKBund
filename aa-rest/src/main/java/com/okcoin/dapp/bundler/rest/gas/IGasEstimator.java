package com.okcoin.dapp.bundler.rest.gas;


import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.util.MathUtil;
import com.okcoin.dapp.bundler.rest.constant.GasConstant;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import java.math.BigInteger;

import static com.okcoin.dapp.bundler.rest.constant.GasConstant.CALL_GAS_LIMIT_FAKE;
import static com.okcoin.dapp.bundler.rest.constant.GasConstant.VERIFICATION_GAS_LIMIT_FAKE;

public interface IGasEstimator {

    boolean fit(IChain chain);

    default UserOperationGasDO estimateGas(UserOperationDO uop) {
        UserOperationGasDO gasFromEvm = estimateGasFromEvm(uop);
        BigInteger verificationGasLimit = gasFromEvm.getVerificationGasLimit();
        BigInteger callGasLimit = MathUtil.max(gasFromEvm.getCallGasLimit(), estimateCallGasLimitFromNode(uop));
        BigInteger preVerificationGas = estimatePreVerificationGas(uop);
        BigInteger preVerificationGasForL1 = estimatePreVerificationGasForL1(uop);
        long validAfter = gasFromEvm.getValidAfter();
        long validUntil = gasFromEvm.getValidUntil();

        return new UserOperationGasDO(preVerificationGas.add(preVerificationGasForL1), verificationGasLimit, callGasLimit,
                validAfter, validUntil);
    }

    default void fillGasForEstimatePreVerificationGas(UserOperationDO uop) {
        uop.setMaxFeePerGas(GasConstant.MAX_FEE_PER_GAS_FAKE);
        uop.setMaxPriorityFeePerGas(GasConstant.MAX_PRIORITY_FEE_PER_GAS_FAKE);
        uop.setPreVerificationGas(GasConstant.PRE_VERIFICATION_GAS_FAKE);
        uop.setVerificationGasLimit(VERIFICATION_GAS_LIMIT_FAKE);
        uop.setCallGasLimit(CALL_GAS_LIMIT_FAKE);
        int sigSize = MathUtil.max(uop.getSignature().length() - 2, 65 * 2);
        uop.setSignature(Numeric.prependHexPrefix(Strings.repeat('f', sigSize)));
    }

    UserOperationGasDO estimateGasFromEvm(UserOperationDO uop);

    BigInteger estimateCallGasLimitFromNode(UserOperationDO uop);

    BigInteger estimatePreVerificationGas(UserOperationDO uop);

    BigInteger estimatePreVerificationGasForL1(UserOperationDO uop);

}
