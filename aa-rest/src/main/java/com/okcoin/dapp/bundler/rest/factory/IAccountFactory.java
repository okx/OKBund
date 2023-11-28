package com.okcoin.dapp.bundler.rest.factory;

import com.okcoin.dapp.bundler.infra.chain.IChain;

public abstract class IAccountFactory {

    public abstract String getInitCode(InitCodeContext context);

    public abstract String getSenderAddress(SenderAddressContext context, IChain chain);
}
