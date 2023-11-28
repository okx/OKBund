package com.okcoin.dapp.bundler.infra.chain;

import com.google.common.collect.Iterables;
import com.okcoin.dapp.bundler.infra.chain.constant.Web3Constant;
import com.okcoin.dapp.bundler.infra.chain.web3j.req.StateOverride;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class TransactionUtil {

    //region execute
    @SneakyThrows
    public static EthSendTransaction sendTransaction(IChain chain, String from, BigInteger nonce, BigInteger gasLimit,
                                                     String to, BigInteger value, String data, BigInteger... gasPrice) {
        EthSendTransaction ethSendTransaction;
        if (FieldUtil.isValidAddress(from)) {
            Transaction transaction;
            if (chain.isEip1559()) {
                transaction = new Transaction(from, nonce, null, gasLimit, to, value, data, chain.getChainId(), gasPrice[1], gasPrice[0]);
            } else {
                transaction = new Transaction(from, nonce, gasPrice[0], gasLimit, to, value, data, chain.getChainId(), null, null);
            }
            ethSendTransaction = chain.getWeb3j().ethSendTransaction(transaction).send();
        } else {
            RawTransactionManager transactionManager;
            RawTransaction rawTransaction;
            Credentials credentials = Credentials.create(from);
            if (chain.isEip1559()) {
                transactionManager = new RawTransactionManager(chain.getWeb3j(), credentials, chain.getChainId());
                rawTransaction = RawTransaction.createTransaction(chain.getChainId(), nonce, gasLimit, to, value, data, gasPrice[1], gasPrice[0]);

            } else {
                transactionManager = new RawTransactionManager(chain.getWeb3j(), credentials, chain.getChainId());
                rawTransaction = RawTransaction.createTransaction(nonce, gasPrice[0], gasLimit, to, value, data);
            }
            ethSendTransaction = transactionManager.signAndSend(rawTransaction);
        }
        ChainErrorUtil.throwChainError(ethSendTransaction);
        return ethSendTransaction;
    }

    public static EthSendTransaction execute(IChain chain, String from, BigInteger gasLimit, String to, BigInteger value,
                                             String data,
                                             boolean isAccelerated, BigInteger... prices) {
        String fromAddr = from;
        if (!FieldUtil.isValidAddress(from)) {
            fromAddr = Credentials.create(from).getAddress();
        }
        if (gasLimit == null) {
            gasLimit = estimateGas(fromAddr, to, data, chain).multiply(BigInteger.valueOf(13)).divide(BigInteger.TEN);
        }
        BigInteger nonce = AccountUtil.getNonce(fromAddr, isAccelerated, chain);
        prices = resolveGasPrice(chain, prices);
        return sendTransaction(chain, from, nonce, gasLimit, to, value, data, prices);
    }

    public static EthSendTransaction execute(IChain chain, String from, String to, BigInteger value, String data,
                                             boolean isAccelerated, BigInteger... prices) {
        return execute(chain, from, null, to, value, data, isAccelerated, prices);
    }

    public static EthSendTransaction execute(IChain chain, String from, String to, BigDecimal value, String data) {
        return execute(chain, from, to, ConvertUtil.fromEther(value), data, false);
    }

    public static EthSendTransaction execute(IChain chain, String from, String to, String data) {
        return execute(chain, from, to, null, data, false);
    }

    public static EthSendTransaction transfer(IChain chain, String from, String to, BigDecimal value) {
        return execute(chain, from, to, Convert.toWei(value, Convert.Unit.ETHER).toBigIntegerExact(), "", false);
    }

    //endregion

    //region call
    @SneakyThrows
    public static EthCall call(String from, BigInteger nonce, BigInteger gasLimit, String to,
                               BigInteger value, String data, IChain chain, BigInteger blockNumber, StateOverride stateOverride, BigInteger... prices) {
        Transaction transaction;
        if (prices.length == 0) {
            transaction = Transaction.createFunctionCallTransaction(from, nonce, null, gasLimit, to, value, data);
        } else if (chain.isEip1559()) {
            transaction = new Transaction(from, nonce, null, gasLimit, to, value, data, chain.getChainId(), prices[1], prices[0]);
        } else {
            transaction = Transaction.createFunctionCallTransaction(from, nonce, prices[0], gasLimit, to, value, data);
        }

        DefaultBlockParameter defaultBlockParameter;
        if (blockNumber == null) {
            defaultBlockParameter = DefaultBlockParameterName.LATEST;
        } else {
            defaultBlockParameter = DefaultBlockParameter.valueOf(blockNumber);
        }
        return new Request<>(
                "eth_call",
                Arrays.asList(transaction, defaultBlockParameter, stateOverride),
                chain.getWeb3j().getWeb3jService(),
                EthCall.class).send();
    }

    public static EthCall call(String from, BigInteger gasLimit, String to, String data, IChain chain) {
        return call(from, null, gasLimit, to, null, data, chain, null, null);
    }

    public static EthCall call(String from, String to, String data, IChain chain) {
        return call(from, null, null, to, null, data, chain, null, null);
    }

    public static EthCall call(String from, String to, String data, IChain chain, StateOverride stateOverride) {
        return call(from, null, null, to, null, data, chain, null, stateOverride);
    }
    //endregion

    //region estimateGas
    @SneakyThrows
    public static BigInteger estimateGas(Transaction transaction, IChain chain) {
        EthEstimateGas ethEstimateGas = chain.getWeb3j().ethEstimateGas(transaction).send();
        ChainErrorUtil.throwChainError(ethEstimateGas);
        return ethEstimateGas.getAmountUsed();
    }

    public static BigInteger estimateGas(String from, String to, String data, IChain chain) {
        Transaction transaction = Transaction.createEthCallTransaction(from, to, data);
        return estimateGas(transaction, chain);
    }
    //endregion

    @SneakyThrows
    public static BigInteger getGasPrice(IChain chain) {
        EthGasPrice ethGasPrice = chain.getWeb3j().ethGasPrice().send();
        ChainErrorUtil.throwChainError(ethGasPrice);
        return ethGasPrice.getGasPrice();
    }

    @SneakyThrows
    public static BigInteger getMaxPriorityFeePerGas(IChain chain) {
        EthMaxPriorityFeePerGas ethMaxPriorityFeePerGas = chain.getWeb3j().ethMaxPriorityFeePerGas().send();
        ChainErrorUtil.throwChainError(ethMaxPriorityFeePerGas);
        return ethMaxPriorityFeePerGas.getMaxPriorityFeePerGas();
    }

    @SneakyThrows
    public static ChainFee get1559GasPrice(IChain chain, Double rewardPercentile) {
        EthFeeHistory ethFeeHistory = chain.getWeb3j().ethFeeHistory(2, DefaultBlockParameterName.PENDING, Collections.singletonList(rewardPercentile)).send();
        ChainErrorUtil.throwChainError(ethFeeHistory);
        EthFeeHistory.FeeHistory feeHistory = ethFeeHistory.getFeeHistory();
        BigInteger lastBaseFee = Iterables.getLast(feeHistory.getBaseFeePerGas());
        List<BigInteger> rewards = Iterables.getLast(feeHistory.getReward());
        return new ChainFee(lastBaseFee, Iterables.getLast(rewards));
    }

    private static BigInteger[] resolveGasPrice(IChain chain, BigInteger... prices) {
        if (chain.isEip1559()) {
            if (prices.length < 2) {
                ChainFee gasPrice1559 = get1559GasPrice(chain, Web3Constant.FEE_HISTORY_COMMON_REWARD_PERCENTILE);
                BigInteger maxPriorityFeePerGas = gasPrice1559.getMaxPriorityFeePerGas();
                BigInteger baseFee = gasPrice1559.getBaseFee();
                BigInteger maxFeePerGas = maxPriorityFeePerGas.add(baseFee.multiply(BigInteger.valueOf(2)));
                return new BigInteger[]{maxFeePerGas, maxPriorityFeePerGas};
            } else {
                return prices;
            }
        } else {
            if (prices.length < 1) {
                BigInteger gasPrice = getGasPrice(chain);
                return new BigInteger[]{gasPrice};
            } else {
                return new BigInteger[]{prices[0]};
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class ChainFee {

        private final BigInteger baseFee;

        private final BigInteger maxPriorityFeePerGas;
    }

}
