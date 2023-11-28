package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.rest.api.req.UserOperationParam;
import com.okcoin.dapp.bundler.rest.api.resp.EstimateUserOperationGasVO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.fieldchecker.FieldChecker;
import com.okcoin.dapp.bundler.rest.gas.GasEstimatorFactory;
import com.okcoin.dapp.bundler.rest.gas.IGasEstimator;
import com.okcoin.dapp.bundler.rest.gas.UserOperationGasDO;
import com.okcoin.dapp.bundler.rest.util.UopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_ESTIMATE_USER_OPERATION_GAS;


@Service(ETH_ESTIMATE_USER_OPERATION_GAS)
@Slf4j
public class EstimateUserOperationGasProcessor implements AAMethodProcessor {

    @Autowired
    private GasEstimatorFactory gasEstimatorFactory;

    @Autowired
    private FieldChecker fieldChecker;

    public EstimateUserOperationGasVO process(IChain chain, List<Object> params) {
        String entryPointAddress = ((String) Iterables.get(params, 1)).toLowerCase();
        UserOperationParam uopParam = JSON.parseObject(JSON.toJSONString(Iterables.get(params, 0)),
                UserOperationParam.class);
        fieldChecker.check(uopParam, chain, entryPointAddress, true);

        UserOperationDO uop = UopUtil.toUserOperationDO(uopParam, chain, entryPointAddress);
        IGasEstimator gasEstimator = gasEstimatorFactory.get(uop.getChain());
        UserOperationGasDO estimateGas = gasEstimator.estimateGas(uop);

        BigInteger verificationGasLimit = estimateGas.getVerificationGasLimit();
        BigInteger callGasLimit = estimateGas.getCallGasLimit();
        BigInteger preVerificationGas = estimateGas.getPreVerificationGas();

        long validAfter = estimateGas.getValidAfter();
        long validUntil = estimateGas.getValidUntil();
        return new EstimateUserOperationGasVO(preVerificationGas, verificationGasLimit, callGasLimit, validAfter, validUntil);
    }
}
