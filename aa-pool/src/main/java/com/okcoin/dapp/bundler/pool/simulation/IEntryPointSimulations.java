package com.okcoin.dapp.bundler.pool.simulation;

import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.ReferencedCodeHashes;
import com.okcoin.dapp.bundler.pool.domain.debug.SimulateValidationResult;
import com.okcoin.dapp.bundler.pool.domain.error.SimulateHandleOpResultOKX;

public interface IEntryPointSimulations {

    SimulateHandleOpResultOKX simulateHandleOp(UserOperationDO uop, boolean isEstimate);

    SimulateValidationResult simulateValidation(UserOperationDO uop, ReferencedCodeHashes previousCodeHashes);

}
