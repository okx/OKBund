package com.okcoin.dapp.bundler.rest.account;

import java.util.List;

public interface IAccount {

    String getInitFunc(String owner);

    String getCallData(List<SingleCallDataContext> context);

    String sign(AccountSignContext context);
}
