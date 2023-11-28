package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.esaulpaugh.headlong.abi.Tuple;
import com.okcoin.dapp.bundler.infra.chain.CodecUtil;
import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.ReceiptUtil;
import com.okcoin.dapp.bundler.infra.storage.dao.UopAndTxMapDAO;
import com.okcoin.dapp.bundler.pool.config.AbiConfig;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.constant.Eip4377MethodConstant;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.UnexpectedException;
import com.okcoin.dapp.bundler.rest.api.resp.Null;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import com.okcoin.dapp.bundler.rest.util.UopUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.List;
import java.util.Locale;

import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.METHOD_NOT_SUPPORT;
import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_GET_USER_OPERATION_BY_HASH;

@Slf4j
@Service(ETH_GET_USER_OPERATION_BY_HASH)
public class GetUserOperationByHashProcessor implements AAMethodProcessor {

    @Autowired
    private UopAndTxMapDAO uopAndTxMapDAO;

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private AbiConfig abiConfig;


    public Object process(IChain chain, List<Object> params) {
        if (!FieldUtil.isValidBytes32((String) params.get(0))) {
            throw new AAException(METHOD_NOT_SUPPORT, "Missing/invalid userOpHash");
        }
        String opHash = ((String) params.get(0)).toLowerCase(Locale.ROOT);
        String txHash = getTxHashByOpHash(opHash);
        if (txHash == null) {
            log.info("not find uop mapping, opHash:{}, tx: {}", opHash, txHash);
            return Null.NULL;
        }
        Transaction transaction = ReceiptUtil.getTransactionByHash(txHash, chain);
        if (transaction == null) {
            log.info("not find uop on chain, opHash:{}, tx: {}", opHash, txHash);
            return Null.NULL;
        }

        if (!transaction.getTo().equals(poolConfig.getEntrypoint())) {
            throw new UnexpectedException("unable to parse transaction");
        }

        Tuple[] tuples = CodecUtil.decodeFunctionOrError(transaction.getInput(),
                abiConfig.getFunc(Eip4377MethodConstant.HANDLE_OPS).getCanonicalSignature()).get(0);

        if (tuples.length == 0) {
            throw new UnexpectedException("failed to parse transaction");
        }


        UserOperationDO uopResult = null;
        for (Tuple tuple : tuples) {
            UserOperationDO uop = UopUtil.toUserOperationDO(chain, transaction.getTo(), tuple);
            if (uop.getOpHash().equals(opHash)) {
                uopResult = uop;
            }
        }

        if (uopResult == null) {
            throw new UnexpectedException("unable to find userOp in transaction");

        }

        return UopUtil.toUserOperationVo(transaction, uopResult);
    }

    private String getTxHashByOpHash(String opHash) {
        return uopAndTxMapDAO.getTxHashBy(opHash);
    }
}
