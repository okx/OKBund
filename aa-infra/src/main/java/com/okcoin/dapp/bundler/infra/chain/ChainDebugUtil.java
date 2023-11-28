package com.okcoin.dapp.bundler.infra.chain;

import com.alibaba.fastjson2.JSON;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceCallConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TraceConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TracerConfig;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.TracerTypeEnum;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.CallTraceTransaction;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceCall;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.DebugTraceTransaction;
import lombok.SneakyThrows;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;

import java.math.BigInteger;

public class ChainDebugUtil {

    @SneakyThrows
    public static CallTraceTransaction callTraceTransaction(String txHash, boolean onlyTopCall, IChain chain) {
        TraceConfig traceConfig = new TraceConfig();
        TracerConfig tracerConfig = new TracerConfig();
        tracerConfig.setOnlyTopCall(onlyTopCall);
        traceConfig.setTracerConfig(tracerConfig);
        traceConfig.setTracer(TracerTypeEnum.CALL_TRACER.getType());
        DebugTraceTransaction debugTraceTransaction = chain.getWeb3j().debugTraceTransaction(txHash, traceConfig).send();
        ChainErrorUtil.throwChainError(debugTraceTransaction);

        return JSON.to(CallTraceTransaction.class, debugTraceTransaction.getTraceTransaction().orElse(null));
    }

    @SneakyThrows
    public static <T> T traceCall(String from, String to, String data, BigInteger gasLimit, IChain chain, String tracer, Class<T> klass) {
        TraceCallConfig traceCallConfig = new TraceCallConfig();
        traceCallConfig.setTracer(tracer);

        Transaction transaction = Transaction.createFunctionCallTransaction(from, null, null, gasLimit, to, data);
        DebugTraceCall debugTraceCall = chain.getWeb3j().debugTraceCall(transaction, DefaultBlockParameterName.LATEST, traceCallConfig).send();
        ChainErrorUtil.throwChainError(debugTraceCall);

        return JSON.to(klass, debugTraceCall.getTraceCall().orElse(null));
    }
}
