package com.okcoin.dapp.bundler.rest.paymaster;


import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;

public interface IPaymaster {

    boolean fit(IChain chain, String paymasterAddress);

    String fakeSign(UserOperationDO uop);
}
