package com.okcoin.dapp.bundler.rest.account;

import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.web3j.crypto.Credentials;

@Getter
@AllArgsConstructor
public class AccountSignContext {

    private final UserOperationDO uop;

    private final Credentials credentials;

}
