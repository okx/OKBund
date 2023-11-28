package com.okcoin.dapp.bundler.infra.chain.web3j;

import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceCallConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceCall;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceTransaction;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;

import java.util.concurrent.ScheduledExecutorService;

public interface Web3jDebug extends Admin {

    static JsonRpc2_0Debug build(Web3jService web3jService) {
        return new JsonRpc2_0Debug(web3jService);
    }

    static JsonRpc2_0Debug build(
            Web3jService web3jService,
            long pollingInterval,
            ScheduledExecutorService scheduledExecutorService) {
        return new JsonRpc2_0Debug(web3jService, pollingInterval, scheduledExecutorService);
    }

    <T> Request<?, DebugTraceTransaction> debugTraceTransaction(String txHash, TraceConfig traceConfig);

    <T> Request<?, DebugTraceCall> debugTraceCall(Transaction transaction,
                                                  DefaultBlockParameter defaultBlockParameter,
                                                  TraceCallConfig traceConfig);

    Web3jService getWeb3jService();
}
