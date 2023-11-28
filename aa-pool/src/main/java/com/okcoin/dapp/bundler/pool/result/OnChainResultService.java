package com.okcoin.dapp.bundler.pool.result;

import com.okcoin.dapp.bundler.infra.storage.DTO.UopAndTxMapEntity;
import com.okcoin.dapp.bundler.infra.storage.dao.UopAndTxMapDAO;
import com.okcoin.dapp.bundler.pool.domain.event.AccountDeployedEvent;
import com.okcoin.dapp.bundler.pool.domain.event.UserOperationEvent;
import com.okcoin.dapp.bundler.pool.mem.MempoolService;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @Author fanweiqiang
 * @create 2023/10/25 14:11
 */
@Slf4j
@Service
public class OnChainResultService {

    @Resource
    private UopAndTxMapDAO uopAndTxMapDAO;
    @Resource
    private ReputationService reputationService;
    @Resource
    private MempoolService mempoolService;

    public boolean processLogEvent(Log logEvent) {
        if (UserOperationEvent.isMatch(logEvent)) {
            processUopEvent(new UserOperationEvent(logEvent));
        } else {
            processDeployAccountEvent(new AccountDeployedEvent(logEvent));
        }
        // TODO YUKINO 2023/10/31: getEventAggregator
        return true;
    }

    private void processUopEvent(UserOperationEvent uopEvent) {
        String sender = uopEvent.getSender();
        String txHash = uopEvent.getTransactionHash();
        String uopHash = uopEvent.getUserOpHash();
        String paymaster = uopEvent.getPaymaster();
        log.info("receive uop event for sender: {}, uop_hash: {}, tx_hash: {}", sender, uopHash, txHash);
        UopAndTxMapEntity entity = new UopAndTxMapEntity();
        entity.setOpHash(uopHash);
        entity.setTxHash(txHash);
        uopAndTxMapDAO.save(entity);
        log.info("add opIncluded for sender: {}, paymaster: {}, by uop_hash: {}", sender, paymaster, uopHash);
        updateAddressIncluded(sender, uopEvent.getPaymaster());
        //remove uop
        mempoolService.removeUop(uopEvent.getSender(), uopEvent.getNonce());
    }

    private void processDeployAccountEvent(AccountDeployedEvent event) {
        log.info("process account map factory event for hash: {}, account: {}, factory: {}", event.getTransactionHash(),
                event.getSender(), event.getFactory());
        updateAddressIncluded(event.getFactory());
    }

    private void updateAddressIncluded(String... addressArr) {
        Arrays.stream(addressArr).forEach(address -> reputationService.updateIncludedStatus(address));
    }

}
