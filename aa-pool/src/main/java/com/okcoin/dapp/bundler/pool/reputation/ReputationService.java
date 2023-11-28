package com.okcoin.dapp.bundler.pool.reputation;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.okcoin.dapp.bundler.pool.config.ReputationConfig;
import com.okcoin.dapp.bundler.pool.domain.error.StakeInfo;
import com.okcoin.dapp.bundler.pool.domain.reputation.ReputationEntryDO;
import com.okcoin.dapp.bundler.pool.domain.reputation.ReputationStatusEnum;
import com.okcoin.dapp.bundler.pool.exception.AAException;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionData;
import com.okcoin.dapp.bundler.pool.exception.AAExceptionEnum;
import com.okcoin.dapp.bundler.pool.util.AddressUtil;
import com.okcoin.dapp.bundler.pool.util.MathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ReputationService {

    @Autowired
    private ReputationConfig reputationConfig;

    private final Map<String, ReputationEntryDO> entries = Maps.newConcurrentMap();

    private final Set<String> whitelist = Sets.newConcurrentHashSet();

    private final Set<String> blackList = Sets.newConcurrentHashSet();


    public void checkStake(String title, StakeInfo info) {
        String addr = info.getAddr();
        if (AddressUtil.isEmpty(addr) || whitelist.contains(addr)) {
            return;
        }

        if (getStatus(addr) == ReputationStatusEnum.BANNED) {
            throw new AAException(new AAExceptionData(title, addr), AAExceptionEnum.REPUTATION, "{} {} is banned", title, addr);
        }


        BigInteger stake = info.getStake();
        BigInteger minStake = reputationConfig.getMinStake();
        if (stake.compareTo(minStake) < 0) {
            if (stake.compareTo(BigInteger.ZERO) == 0) {
                throw new AAException(AAExceptionEnum.INSUFFICIENT_STAKE, "{} {} is unstaked", title, addr);
            } else {
                throw new AAException(AAExceptionEnum.INSUFFICIENT_STAKE, "{} {} stake {} is too low (min={})", title, addr, stake, minStake);
            }
        }

        long unstakeDelaySec = info.getUnstakeDelaySec();
        Long minUnstakeDelay = reputationConfig.getMinUnstakeDelay();
        if (unstakeDelaySec < minUnstakeDelay) {
            throw new AAException(AAExceptionEnum.INSUFFICIENT_STAKE, "{} {} unstake delay {} is too low (min={})",
                    title, addr, unstakeDelaySec, minUnstakeDelay);
        }

    }

    public void updateSeenStatus(String addr) {
        updateSeenStatus(addr, 1);
    }

    public void updateSeenStatus(String address, Integer opsSeen) {
        update(address, opsSeen, 0, false);
    }


    public void updateIncludedStatus(String address) {
        update(address, 0, 1, false);
    }

    public void clearState() {
        entries.clear();
    }

    public ReputationStatusEnum getStatus(String address) {
        if (AddressUtil.isEmpty(address) || whitelist.contains(address)) {
            return ReputationStatusEnum.OK;
        }

        if (blackList.contains(address)) {
            return ReputationStatusEnum.BANNED;
        }
        ReputationEntryDO entry = entries.get(address);
        if (entry == null) {
            return ReputationStatusEnum.OK;
        }

        long minInclusionDenominator = entry.getOpsSeen() / reputationConfig.getMinInclusionDenominator();
        long throttlingSlack = reputationConfig.getThrottlingSlack();
        long banSlack = reputationConfig.getBanSlack();
        if (minInclusionDenominator <= entry.getOpsIncluded() + throttlingSlack) {
            return ReputationStatusEnum.OK;
        }

        if (minInclusionDenominator <= entry.getOpsIncluded() + banSlack) {
            return ReputationStatusEnum.THROTTLED;
        }

        return ReputationStatusEnum.BANNED;
    }


    /***
     * dump reputation
     * @return
     */
    public List<ReputationEntryDO> dumpReputation() {
        if (MapUtils.isEmpty(entries)) {
            return Lists.newArrayList();
        }


        return Lists.newArrayList(entries.values());
    }

    public int calculateMaxAllowedMempoolOpsUnstaked(String addr) {
        int sameUnstakedEntityMempoolCount = 10;

        ReputationEntryDO entry = entries.get(addr);
        if (entry == null) {
            return sameUnstakedEntityMempoolCount;
        }
        int inclusionRate;
        int inclusionRateFactor = 10;
        if (entry.getOpsSeen() == 0) {
            inclusionRate = 0;
        } else {
            inclusionRate = entry.getOpsIncluded() / entry.getOpsSeen();
        }

        return sameUnstakedEntityMempoolCount + inclusionRate * inclusionRateFactor + MathUtil.min(entry.getOpsIncluded(), 10000);
    }

    public void checkBanned(String title, StakeInfo info) {
        if (getStatus(info.getAddr()) == ReputationStatusEnum.BANNED) {
            throw new AAException(new AAExceptionData(title, info.getAddr()), AAExceptionEnum.REPUTATION, "{} {} is banned", title, info.getAddr());
        }
    }

    public void checkThrottled(String title, StakeInfo info) {
        if (getStatus(info.getAddr()) == ReputationStatusEnum.THROTTLED) {
            throw new AAException(new AAExceptionData(title, info.getAddr()), AAExceptionEnum.REPUTATION, "{} {} is throttled", title, info.getAddr());
        }
    }

    public void update(String addr, int opsSeen, int opsIncluded, boolean isReset) {
        if (AddressUtil.isEmpty(addr)) {
            return;
        }
        entries.compute(addr, (k, v) -> {
            if (v == null) {
                if (opsSeen == 0 && opsIncluded == 0) {
                    return null;
                }
                ReputationEntryDO entry = ReputationEntryDO.newReputationEntryDO(addr);
                entry.setOpsSeen(opsSeen);
                entry.setOpsIncluded(opsIncluded);
                return entry;
            } else {
                int opsSeenNew;
                int opsIncludedNew;
                if (isReset) {
                    opsSeenNew = opsSeen;
                    opsIncludedNew = opsIncluded;
                } else {
                    opsSeenNew = v.getOpsSeen() + opsSeen;
                    opsIncludedNew = v.getOpsIncluded() + opsIncluded;
                }

                if (opsSeenNew == 0 && opsIncludedNew == 0) {
                    return null;
                }
                v.setOpsSeen(opsSeenNew);
                v.setOpsIncluded(opsIncludedNew);
                return v;
            }
        });
    }

    public void decreaseSeenAndIncluded() {
        entries.forEach((k, v) -> {
            int newSeen = v.getOpsSeen() * 23 / 24;
            int newIncluded = v.getOpsIncluded() * 23 / 24;
            update(k, newSeen, newIncluded, true);
        });
    }
}
