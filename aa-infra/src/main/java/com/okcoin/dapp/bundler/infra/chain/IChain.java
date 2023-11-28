package com.okcoin.dapp.bundler.infra.chain;

import com.okcoin.dapp.bundler.infra.chain.web3j.Web3jDebug;


public interface IChain {

    long getChainId();

    boolean isEip1559();

    Web3jDebug getWeb3j();

}
