package com.okcoin.dapp.bundler.pool.result;

import com.okcoin.dapp.bundler.infra.chain.ChainDebugUtil;
import com.okcoin.dapp.bundler.infra.chain.exception.ChainException;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.CallTraceTransaction;
import com.okcoin.dapp.bundler.infra.chain.web3j.resp.TransactionReceiptCommon;
import com.okcoin.dapp.bundler.pool.config.ChainConfig;
import com.okcoin.dapp.bundler.pool.config.ReputationConfig;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.error.FailedOp;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * @Author fanweiqiang
 * @create 2023/10/26 18:15
 */
@Service
@Slf4j
public class OnChainTxFailedService {

    @Resource
    private ChainConfig chainConfig;

    @Resource
    private ReputationService reputationService;

    @Autowired
    private ReputationConfig reputationConfig;

    public void processSingleTx(TransactionReceiptCommon receipt, List<UserOperationDO> uopList) {
        String txHash = receipt.getTransactionHash();
        if (receipt.isStatusOK()) {
            log.info("begin to process failed tx, but tx is success, so skip, tx: {}", txHash);
            return;
        }
        String opRevertReason = getRevertReason(txHash);
        if (!opRevertReason.startsWith(FailedOp.ERROR_METHOD_ID)) {
            log.info("process failed tx, but revert reason methodId is not match, so skip, tx: {}", txHash);
            return;
        }
        FailedOp failedOp = new FailedOp(opRevertReason);
        int opIndex = failedOp.getOpIndex();
        String reason = failedOp.getReason();
        log.info("tx exe failed, tx: {}, failed op index: {}, reason: {}", txHash, opIndex, reason);
        UserOperationDO revertUop = uopList.get(opIndex);
        reputationService.updateSeenStatus(revertUop.getSender(), reputationConfig.getRevertOpAddReputationSeen());
    }

    private String getRevertReason(String txHash) {
        CallTraceTransaction callTraceTransaction = debugCallTraceTransaction(txHash);
        if (callTraceTransaction == null) {
            log.warn("get tx CallTraceTransaction failed, so skip, tx: {}", txHash);
            return "0x";
        }
        String output = callTraceTransaction.getOutput();
        log.info("debugCallTraceTransaction succeed, txHash={}, output={}", txHash, output);
        return output;
    }

    private CallTraceTransaction debugCallTraceTransaction(String txHash) {
        try {
            return ChainDebugUtil.callTraceTransaction(txHash, true, chainConfig);
        } catch (ChainException e) {
            log.error("debugCallTraceTransaction error, txHash={}, msg={}", txHash, e.getMessage());
            return null;
        }
    }

}
