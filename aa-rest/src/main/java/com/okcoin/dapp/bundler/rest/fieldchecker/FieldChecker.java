package com.okcoin.dapp.bundler.rest.fieldchecker;

import com.alibaba.fastjson2.JSON;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.gasprice.GasPriceInfo;
import com.okcoin.dapp.bundler.pool.gasprice.GasService;
import com.okcoin.dapp.bundler.rest.api.req.UserOperationParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

import static com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant.ADDRESS_WITH_HEX_PREFIX;
import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.INVALID_FIELDS;
import static com.okcoin.dapp.bundler.rest.constant.UopFieldConstant.*;


@Component
@Slf4j
public class FieldChecker {

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private GasService gasService;


    /**
     * Do not change the order.
     */
    public void check(UserOperationParam uop, IChain chain, String entrypoint, boolean isEstimate) {
        log.info("FieldChecker start");
        if (uop == null) {
            throw new AAException(AAExceptionEnum.INVALID_FIELDS, "No UserOperation param");
        }
        checkChain(chain);
        checkEntryPoint(entrypoint);
        checkSender(uop);
        checkNonce(uop);
        checkInitCode(uop);
        checkCallData(uop);
        checkPaymasterAndData(uop);
        checkSignature(uop);
        checkPreVerificationGas(uop);
        checkVerificationGasLimit(uop);
        checkCallGasLimit(uop);
        checkMaxFeePerGas(uop);
        checkMaxPriorityFeePerGas(uop);
        checkGas(uop, chain, entrypoint, isEstimate);
        log.info("FieldChecker end");
    }


    private void checkChain(IChain chain) {
        if (poolConfig.getChainId() != chain.getChainId()) {
            // TODO YUKINO 2023/10/24: 校验链id
        }
    }

    private void checkEntryPoint(String entrypoint) {
        if (!StringUtils.startsWith(entrypoint, Web3Constant.HEX_PREFIX)) {
            throw new AAException(AAExceptionEnum.INVALID_FIELDS, "No entryPoint param");
        }
        if (!poolConfig.getEntrypoint().contains(entrypoint)) {
            throw new AAException(INVALID_FIELDS, "The EntryPoint at {} is not supported. This bundler uses {}", entrypoint, poolConfig.getEntrypoint());
        }
    }

    private void checkSender(UserOperationParam uop) {
        checkField(uop, SENDER, uop.getSender());

    }

    private void checkNonce(UserOperationParam uop) {
        checkField(uop, NONCE, uop.getNonce());
    }

    private void checkInitCode(UserOperationParam uop) {
        checkField(uop, INIT_CODE, uop.getInitCode());

        if (uop.getInitCode().length() > 2 && uop.getInitCode().length() < ADDRESS_WITH_HEX_PREFIX) {
            throw new AAException(INVALID_FIELDS, "initCode: must contain at least an address");
        }
    }

    private void checkCallData(UserOperationParam uop) {
        checkField(uop, CALL_DATA, uop.getCallData());

    }

    private void checkPaymasterAndData(UserOperationParam uop) {
        checkField(uop, PAYMASTER_AND_DATA, uop.getPaymasterAndData());
        if (uop.getPaymasterAndData().length() > 2 && uop.getPaymasterAndData().length() < ADDRESS_WITH_HEX_PREFIX) {
            throw new AAException(INVALID_FIELDS, "paymasterAndData: must contain at least an address");
        }
    }

    private void checkSignature(UserOperationParam uop) {
        checkField(uop, SIGNATURE, uop.getSignature());
    }

    private void checkPreVerificationGas(UserOperationParam uopParam) {
        checkField(uopParam, PRE_VERIFICATION_GAS, uopParam.getPreVerificationGas());
    }

    private void checkVerificationGasLimit(UserOperationParam uop) {
        checkField(uop, VERIFICATION_GAS_LIMIT, uop.getVerificationGasLimit());
    }


    private void checkCallGasLimit(UserOperationParam uop) {
        checkField(uop, CALL_GAS_LIMIT, uop.getCallGasLimit());

    }

    private void checkMaxFeePerGas(UserOperationParam uop) {
        checkField(uop, MAX_FEE_PER_GAS, uop.getMaxFeePerGas());

    }

    private void checkMaxPriorityFeePerGas(UserOperationParam uop) {
        checkField(uop, MAX_PRIORITY_FEE_PER_GAS, uop.getMaxPriorityFeePerGas());

    }

    private void checkGas(UserOperationParam uopParam, IChain chain, String entrypoint, boolean isEstimate) {
        BigInteger maxFeePerGas = Numeric.decodeQuantity(uopParam.getMaxFeePerGas());
        BigInteger maxPriorityFeePerGas = Numeric.decodeQuantity(uopParam.getMaxPriorityFeePerGas());
        if (maxFeePerGas.compareTo(maxPriorityFeePerGas) < 0) {
            throw new AAException(INVALID_FIELDS, "maxFeePerGas can't be less than maxPriorityFeePerGas");
        }

        if (isEstimate) {
            return;
        }

        GasPriceInfo gasPriceInfo = gasService.getGasPriceInfoWithCache(chain);
        BigInteger baseFeeMin = gasPriceInfo.getBaseFee();
        if (maxFeePerGas.compareTo(baseFeeMin) < 0) {
            throw new AAException(INVALID_FIELDS, "maxFeePerGas too low: expected at least {}", baseFeeMin);
        }
    }

    private void checkField(UserOperationParam uop, String field, String value) {
        if (value == null) {
            throw new AAException(INVALID_FIELDS, "Missing userOp field: {} {}", field, JSON.toJSONString(uop));
        }

        if (!FieldUtil.isValidHex(value)) {
            throw new AAException(INVALID_FIELDS, "Invalid hex value for property {}:{} in UserOp", field, value);
        }
    }
}
