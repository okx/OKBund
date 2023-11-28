package com.okcoin.dapp.bundler.rest.api.service.impl;

import com.okcoin.dapp.bundler.infra.chain.FieldUtil;
import com.okcoin.dapp.bundler.infra.chain.IChain;
import com.okcoin.dapp.bundler.infra.chain.ReceiptUtil;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.TransactionReceiptCommon;
import com.okcoin.dapp.bundler.infra.storage.dao.UopAndTxMapDAO;
import com.okcoin.dapp.bundler.pool.event.EventService;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.UnexpectedException;
import com.okcoin.dapp.bundler.rest.api.resp.Null;
import com.okcoin.dapp.bundler.rest.api.resp.UserOperationReceiptVO;
import com.okcoin.dapp.bundler.rest.api.service.AAMethodProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import static com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum.METHOD_NOT_SUPPORT;
import static com.okcoin.dapp.bundler.rest.constant.AaMethodConstant.ETH_GET_USER_OPERATION_RECEIPT;


@Service(ETH_GET_USER_OPERATION_RECEIPT)
@Slf4j
public class GetUserOperationReceiptProcessor implements AAMethodProcessor {

    @Autowired
    private UopAndTxMapDAO uopAndTxMapDAO;

    @Autowired
    private EventService eventService;

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
        TransactionReceiptCommon receipt = ReceiptUtil.getTransactionReceipt(txHash, chain);
        if (receipt == null) {
            log.info("not find uop on chain, opHash:{}, tx: {}", opHash, txHash);
            return Null.NULL;
        }

        LinkedHashMap<String, List<Log>> logMap = eventService.groupUserOperationLog(receipt.getTo(), receipt.getLogs());

        List<Log> logs = logMap.get(opHash);
        if (CollectionUtils.isEmpty(logs)) {
            throw new UnexpectedException("fatal: no UserOperationEvent in logs");
        }
        return UserOperationReceiptVO.dealUserOperationReceiptVo(receipt, opHash, logs);
    }

    private String getTxHashByOpHash(String opHash) {
        return uopAndTxMapDAO.getTxHashBy(opHash);
    }
}
