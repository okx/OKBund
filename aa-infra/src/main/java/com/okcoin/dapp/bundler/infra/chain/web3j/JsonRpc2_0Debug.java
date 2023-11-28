package com.okcoin.dapp.bundler.infra.chain.web3j;

import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceCallConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceCall;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceTransaction;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.JsonRpc2_0Admin;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

public class JsonRpc2_0Debug extends JsonRpc2_0Admin implements Web3jDebug {

    public JsonRpc2_0Debug(Web3jService web3jService) {
        super(web3jService);
    }

    public JsonRpc2_0Debug(Web3jService web3jService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        super(web3jService, pollingInterval, scheduledExecutorService);
    }

    @Override
    public <T> Request<?, DebugTraceTransaction> debugTraceTransaction(String txHash, TraceConfig traceConfig) {
        return new Request<>("debug_traceTransaction", Arrays.asList(txHash, traceConfig), web3jService, DebugTraceTransaction.class);
    }

    @Override
    public <T> Request<?, DebugTraceCall> debugTraceCall(Transaction transaction, DefaultBlockParameter defaultBlockParameter, TraceCallConfig traceConfig) {
        return new Request<>("debug_traceCall", Arrays.asList(transaction, defaultBlockParameter, traceConfig), web3jService, DebugTraceCall.class);
    }

    public Web3jService getWeb3jService() {
        return web3jService;
    }
}
