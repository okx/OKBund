package com.okcoin.dapp.bundler.pool.mem;

import com.google.common.collect.Sets;
import com.okcoin.dapp.bundler.pool.config.ReputationConfig;
import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.SimulateValidationResult;
import com.okcoin.dapp.bundler.pool.domain.error.StakeInfo;
import com.okcoin.dapp.bundler.pool.domain.pool.AddressCountDO;
import com.okcoin.dapp.bundler.pool.domain.pool.MempoolDO;
import com.okcoin.dapp.bundler.pool.domain.pool.MempoolEntry;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.okcoin.dapp.bundler.pool.constant.Eip4377CommonConstant.*;


@Service
@Slf4j
public class MempoolService {

    private final MempoolDO mempool = new MempoolDO();

    private final AddressCountDO entryCount = new AddressCountDO();

    @Autowired
    private ReputationConfig reputationConfig;

    @Autowired
    private ReputationService reputationService;


    public void addUop(UserOperationDO uop, SimulateValidationResult validationResult) {
        String sender = uop.getSender();
        BigInteger nonce = uop.getNonce();
        MempoolEntry entry = new MempoolEntry(uop, validationResult.getReferencedContracts());
        MempoolEntry oldEntry = mempool.get(sender, nonce);
        StakeInfo aggregatorInfo = validationResult.getAggregatorInfo();
        StakeInfo senderInfo = validationResult.getSenderInfo();
        if (oldEntry != null) {
            checkReplaceUserOp(oldEntry, entry);
            mempool.replace(entry, oldEntry);
        } else {
            entryCount.incrementEntryCount(ACCOUNT_TITLE, uop.getSender());
            entryCount.incrementEntryCount(PAYMASTER_TITLE, uop.getPaymaster());
            entryCount.incrementEntryCount(FACTORY_TITLE, uop.getFactory());
            checkReputation(senderInfo, validationResult.getFactoryInfo(),
                    validationResult.getPaymasterInfo(), aggregatorInfo);
            checkMultipleRolesViolation(uop);
            mempool.add(entry);
        }
        updateSeenStatus(aggregatorInfo == null ? null : aggregatorInfo.getAddr(), uop, senderInfo);
    }

    private void updateSeenStatus(String aggregator, UserOperationDO uop, StakeInfo senderInfo) {
        try {
            reputationService.checkStake(ACCOUNT_TITLE, senderInfo);
            reputationService.updateSeenStatus(uop.getSender());
        } catch (AAException e) {
            log.error("account check stake error: {}", e.getMsg());
        }
        reputationService.updateSeenStatus(aggregator);
        reputationService.updateSeenStatus(uop.getPaymaster());
        reputationService.updateSeenStatus(uop.getFactory());
    }

    private void checkMultipleRolesViolation(UserOperationDO uop) {
        Set<String> paymasterKnownAddresses = entryCount.knownAddresses(PAYMASTER_TITLE);
        Set<String> factoryKnownAddresses = entryCount.knownAddresses(FACTORY_TITLE);
        String sender = uop.getSender();
        if (paymasterKnownAddresses.contains(sender) || factoryKnownAddresses.contains(sender)) {
            throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "The sender address \"{}\" is used as a different" +
                    " entity in another UserOperation currently in mempool", sender);
        }

        Set<String> senderKnownAddresses = entryCount.knownAddresses(ACCOUNT_TITLE);
        String paymaster = uop.getPaymaster();

        if (senderKnownAddresses.contains(paymaster)) {
            throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "A Paymaster at \"{}\" in this " +
                    "UserOperation is used as a sender entity in another UserOperation currently in mempool.", paymaster);

        }
        String factory = uop.getFactory();
        if (senderKnownAddresses.contains(factory)) {
            throw new AAException(AAExceptionEnum.OPCODE_VALIDATION, "A Factory at \"{}\" in this " +
                    "UserOperation is used as a sender entity in another UserOperation currently in mempool.", factory);
        }
    }


    public void clearState() {
        mempool.clearState();
        entryCount.clearState();
    }

    public List<UserOperationDO> dump() {
        return mempool.getAllEntityByPriority().stream().map(MempoolEntry::getUop).collect(Collectors.toList());
    }

    private void checkReputation(StakeInfo senderInfo, StakeInfo factoryInfo, StakeInfo paymasterInfo, StakeInfo aggregatorInfo) {
        checkReputationStatus(ACCOUNT_TITLE, senderInfo, reputationConfig.getMaxMempoolUserOpsPerSender());
        if (factoryInfo != null) {
            int maxAllowedMempoolOpsUnstaked = reputationService.calculateMaxAllowedMempoolOpsUnstaked(factoryInfo.getAddr());
            checkReputationStatus(FACTORY_TITLE, factoryInfo, maxAllowedMempoolOpsUnstaked);

        }

        if (paymasterInfo != null) {
            int maxAllowedMempoolOpsUnstaked = reputationService.calculateMaxAllowedMempoolOpsUnstaked(paymasterInfo.getAddr());
            checkReputationStatus(PAYMASTER_TITLE, paymasterInfo, maxAllowedMempoolOpsUnstaked);
        }

        if (aggregatorInfo != null) {
            int maxAllowedMempoolOpsUnstaked = reputationService.calculateMaxAllowedMempoolOpsUnstaked(aggregatorInfo.getAddr());
            checkReputationStatus(AGGREGATOR_TITLE, aggregatorInfo, maxAllowedMempoolOpsUnstaked);
        }

    }

    private void checkReputationStatus(String title, StakeInfo stakeInfo, int maxTxMempoolAllowedEntity) {
        reputationService.checkBanned(title, stakeInfo);
        int count = entryCount.entryCount(stakeInfo.getAddr());
        if (count > reputationConfig.getThrottledEntityMempoolCount()) {
            reputationService.checkThrottled(title, stakeInfo);
        }
        if (count > maxTxMempoolAllowedEntity) {
            reputationService.checkStake(title, stakeInfo);
        }
    }

    private void checkReplaceUserOp(MempoolEntry oldEntry, MempoolEntry entry) {
        BigInteger oldMaxPriorityFeePerGas = oldEntry.getUop().getMaxPriorityFeePerGas();
        BigInteger oldMaxFeePerGas = oldEntry.getUop().getMaxFeePerGas();
        BigInteger maxPriorityFeePerGas = entry.getUop().getMaxPriorityFeePerGas();
        BigInteger maxFeePerGas = entry.getUop().getMaxFeePerGas();
        if (maxFeePerGas.compareTo(oldMaxFeePerGas.multiply(BigInteger.valueOf(11)).divide(BigInteger.TEN)) < 0) {
            throw new AAException(AAExceptionEnum.INVALID_FIELDS, "Replacement UserOperation must have higher " + "maxFeePerGas (old={} new={})", oldMaxFeePerGas, maxFeePerGas);
        }

        if (maxPriorityFeePerGas.compareTo(oldMaxPriorityFeePerGas.multiply(BigInteger.valueOf(11)).divide(BigInteger.TEN)) < 0) {
            throw new AAException(AAExceptionEnum.INVALID_FIELDS, "Replacement UserOperation must have higher " + "maxPriorityFeePerGas (old={} new={})", oldMaxPriorityFeePerGas, maxPriorityFeePerGas);
        }
    }

    public int count() {
        return mempool.getMempoolSize();
    }

    public void rePushUop(MempoolEntry entry) {
        mempool.add(entry);
    }

    public void removeUop(String sender, BigInteger nonce) {
        MempoolEntry entry = mempool.remove(sender, nonce);
        if (entry != null) {
            UserOperationDO uop = entry.getUop();
            entryCount.decrementEntryCount(ACCOUNT_TITLE, uop.getSender());
            entryCount.decrementEntryCount(PAYMASTER_TITLE, uop.getPaymaster());
            entryCount.decrementEntryCount(FACTORY_TITLE, uop.getFactory());
        }
    }

    public List<MempoolEntry> getAllEntryWithPriority() {
        return mempool.getAllEntityByPriority();
    }

    public Set<String> getKnownSenders() {
        Set<String> knownSenders = Sets.newHashSet();
        mempool.getAllEntityByPriority().forEach(e -> {
            knownSenders.add(e.getUop().getSender());
        });

        return knownSenders;
    }

    public MempoolEntry getEntry(String sender, BigInteger nonce) {
        return mempool.get(sender, nonce);
    }
}
