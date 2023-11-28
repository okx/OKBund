package com.okcoin.dapp.bundler.rest.account;

import com.okcoin.dapp.bundler.rest.account.simple.AccountSimple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountFactory {

    @Autowired
    private Map<String, IAccount> accountMap;

    public IAccount get(String account) {
        return accountMap.get(AccountSimple.VERSION);

    }
}
