package com.okcoin.dapp.bundler.rest.api.service.impl;


import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.bundler.ExecutionService;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.SimulateValidationResult;
import com.okcoin.dapp.bundler.pool.domain.error.SimulateHandleOpResultOKX;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import com.okcoin.dapp.bundler.pool.simulation.EntryPointSimulationsFactory;
import com.okcoin.dapp.bundler.pool.simulation.IEntryPointSimulations;
import com.okcoin.dapp.bundler.rest.api.req.UserOperationParam;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.fieldchecker.FieldChecker;
import com.okcoin.dapp.bundler.rest.gas.GasEstimatorFactory;
import com.okcoin.dapp.bundler.rest.gas.IGasEstimator;
import com.okcoin.dapp.bundler.rest.util.UopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.INVALID_FIELDS;
import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_SEND_USER_OPERATION;


@Service(ETH_SEND_USER_OPERATION)
@Slf4j
public class SendUserOperationProcessor implements AAMethodProcessor {

    @Autowired
    private EntryPointSimulationsFactory entryPointSimulationsFactory;

    @Autowired
    private FieldChecker fieldChecker;

    @Autowired
    private MempoolService mempoolService;

    @Autowired
    private GasEstimatorFactory gasEstimatorFactory;

    @Autowired
    private ExecutionService executionService;

    @Override
    public String process(IChain chain, List<Object> params) {
        String entryPointAddress = ((String) Iterables.get(params, 1)).toLowerCase();
        UserOperationParam uopParam = JSON.parseObject(JSON.toJSONString(Iterables.get(params, 0)), UserOperationParam.class);
        fieldChecker.check(uopParam, chain, entryPointAddress, false);

        UserOperationDO uop = UopUtil.toUserOperationDO(uopParam, chain, entryPointAddress);
        UserOperationDO uopForCheck = UopUtil.copy(uop);
        IGasEstimator gasEstimator = gasEstimatorFactory.get(chain);
        BigInteger preVerificationGas = uopForCheck.getPreVerificationGas();
        BigInteger preVerificationGasMin = gasEstimator.estimatePreVerificationGas(uopForCheck);
        BigInteger preVerificationGasForL1Min = gasEstimator.estimatePreVerificationGasForL1(uopForCheck);

        if (preVerificationGasMin.add(preVerificationGasForL1Min).compareTo(preVerificationGas) > 0) {
            throw new AAException(INVALID_FIELDS, "preVerificationGas too low: expected at least {}", preVerificationGasMin);
        }

        uop.setPreVerificationGasL1(preVerificationGasForL1Min);
        IEntryPointSimulations entryPointSimulations = entryPointSimulationsFactory.get(entryPointAddress);
        SimulateHandleOpResultOKX handleOpResult = entryPointSimulations.simulateHandleOp(uop, false);
        uop.setGasLimitForPay(handleOpResult.getActualGasUsed());

        SimulateValidationResult simulateValidationResult = entryPointSimulations.simulateValidation(uop, null);
        mempoolService.addUop(uop, simulateValidationResult);
        executionService.attemptBundle(false);
        return uop.getOpHash();
    }

}
