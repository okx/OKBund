package com.okcoin.dapp.bundler.rest.factory;

import com.okcoin.dapp.bundler.rest.factory.simple.AccountFactorySimple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountFactoryFactory {

    @Autowired
    private Map<String, IAccountFactory> accountFactoryMap;

    public IAccountFactory get(String accountFactory) {
        return accountFactoryMap.get(AccountFactorySimple.VERSION);
    }
}
