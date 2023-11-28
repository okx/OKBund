package com.okcoin.dapp.bundler.rest.factory.simple;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.pool.entrypoint.Entrypoint;
import com.okcoin.dapp.bundler.rest.factory.IAccountFactory;
import com.okcoin.dapp.bundler.rest.factory.InitCodeContext;
import com.okcoin.dapp.bundler.rest.factory.SenderAddressContext;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import static com.okcoin.dapp.bundler.rest.factory.simple.AccountFactorySimple.VERSION;


@Service(VERSION)
public class AccountFactorySimple extends IAccountFactory {

    public static final String VERSION = "account_factory_simple";

    @Override
    public String getInitCode(InitCodeContext context) {
        String funcName = "createAccount";
        Function function = new Function(funcName, Lists.newArrayList(new Address(context.getOwner()), new Uint256(context.getSalt())), Lists.newArrayList(TypeReference.create(Address.class)));
        String initCode = FunctionEncoder.encode(function);
        return context.getFactory() + Numeric.cleanHexPrefix(initCode);
    }

    @Override
    public String getSenderAddress(SenderAddressContext context, IChain chain) {
        String entrypoint = context.getEntrypoint();
        return Entrypoint.getSenderAddress(entrypoint, context.getInitCode(), chain);
    }
}
