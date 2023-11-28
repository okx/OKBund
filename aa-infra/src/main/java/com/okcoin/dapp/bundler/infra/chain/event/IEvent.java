package com.okcoin.dapp.bundler.infra.chain.event;

import org.web3j.protocol.core.methods.response.Log;

public interface IEvent {

    Log getLog();

    default String getSig() {
        return getLog().getTopics().get(0);
    }


    default long getBlockNumber() {
        return getLog().getBlockNumber().longValue();
    }

    default int getLogIndex() {
        return getLog().getLogIndex().intValue();
    }

    default String getAddress() {
        return getLog().getAddress();
    }

    default String getTransactionHash() {
        return getLog().getTransactionHash();
    }

    default String getBlockHash() {
        return getLog().getBlockHash();
    }

}
