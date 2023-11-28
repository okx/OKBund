package com.okcoin.dapp.bundler.rest.account.simple;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.WalletUtil;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.rest.account.AccountSignContext;
import com.okcoin.dapp.bundler.rest.account.IAccount;
import com.okcoin.dapp.bundler.rest.account.SingleCallDataContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;

import java.util.List;

import static com.okcoin.dapp.bundler.rest.account.simple.AccountSimple.VERSION;

@Service(VERSION)
public class AccountSimple implements IAccount {

    public static final String VERSION = "account_simple";

    public String getInitFunc(String owner) {
        return Web3Constant.HEX_PREFIX;
    }

    @Override
    public String getCallData(List<SingleCallDataContext> context) {
        if (CollectionUtils.isEmpty(context)) {
            return Web3Constant.HEX_PREFIX;
        }

        Function function;
        if (context.size() == 1) {
            SingleCallDataContext single = context.get(0);
            function = new Function("execute", Lists.newArrayList(new Address(single.getTo()), new Uint256(single.getValue()), new DynamicBytes(single.getData())), Lists.newArrayList());
        } else {
            List<Address> toList = Lists.newArrayList();
            List<DynamicBytes> dataList = Lists.newArrayList();
            for (SingleCallDataContext single : context) {
                toList.add(new Address(single.getTo()));
                dataList.add(new DynamicBytes(single.getData()));
            }
            function = new Function("executeBatch", Lists.newArrayList(new DynamicArray<>(Address.class, toList), new DynamicArray<>(DynamicBytes.class, dataList)), Lists.newArrayList());

        }

        return FunctionEncoder.encode(function);
    }

    @Override
    public String sign(AccountSignContext context) {
        UserOperationDO uop = context.getUop();
        Credentials credentials = context.getCredentials();
        return WalletUtil.signToString(WalletUtil.signPrefixedMessage(uop.getOpHash(), credentials.getEcKeyPair()));
    }
}
