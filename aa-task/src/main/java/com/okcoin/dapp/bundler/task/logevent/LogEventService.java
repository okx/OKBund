package com.okcoin.dapp.bundler.task.logevent;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.ChainUtil;
import com.okcoin.dapp.bundler.infra.chain.ReceiptUtil;
import com.okcoin.dapp.bundler.infra.storage.DTO.BlockHeightSignEntity;
import com.okcoin.dapp.bundler.infra.storage.dao.BlockHeightSignDAO;
import com.okcoin.dapp.bundler.pool.config.ChainConfig;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import com.okcoin.dapp.bundler.pool.domain.event.AccountDeployedEvent;
import com.okcoin.dapp.bundler.pool.domain.event.UserOperationEvent;
import com.okcoin.dapp.bundler.pool.result.OnChainResultService;
import com.okcoin.dapp.bundler.task.constant.LogEventConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.core.methods.response.Log;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author fanweiqiang
 * @create 2023/10/24 17:54
 */
@Slf4j
@Component
public class LogEventService {

    private Long lastProcessBlockHeight;

    @Resource
    private OnChainResultService onChainResultService;
    @Resource
    private BlockHeightSignDAO blockHeightSignDAO;

    @Autowired
    private PoolConfig poolConfig;

    @Autowired
    private ChainConfig chainConfig;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(LogEventConstant.LOG_EVENT_THREAD_POOL_CORE_SIZE,
            LogEventConstant.LOG_EVENT_THREAD_POOL_MAX_SIZE, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(LogEventConstant.LOG_EVENT_THREAD_POOL_MAX_QUEUE_SIZE), (r, executor) -> log.error("log event pool is full, drop task!"));

    @PostConstruct
    private void loadBlockHeightFromDb() {
        BlockHeightSignEntity entity = null;
        long chainId = chainConfig.getChainId();
        try {
            entity = blockHeightSignDAO.getObj(String.valueOf(chainId));
        } catch (Exception e) {
        }
        if (entity == null) {
            log.info("not find block height sign from db for chain: {}", chainId);
            return;
        }
        lastProcessBlockHeight = entity.getBlockHeight();
    }


    // TODO YUKINO 2023/10/30: 循环
    public synchronized void handlePastEvents() {
        long currBlockHeight = ChainUtil.getBlockNumber(chainConfig).longValue();
        if (lastProcessBlockHeight == null) {
            lastProcessBlockHeight = 0L;
        }
        Long blockHeightEnd = Math.min(currBlockHeight, lastProcessBlockHeight + LogEventConstant.PULL_BLOCK_SIZE);
        List<Log> logEvents = ReceiptUtil.getLogs(lastProcessBlockHeight + 1, blockHeightEnd,
                Lists.newArrayList(poolConfig.getEntrypoint()), chainConfig, AccountDeployedEvent.EVENT_SIG, UserOperationEvent.EVENT_SIG);
        if (CollectionUtils.isEmpty(logEvents)) {
            log.info("not find log event from height: {} to: {}", lastProcessBlockHeight, blockHeightEnd);
            lastProcessBlockHeight = blockHeightEnd;
            return;
        }
        List<Callable<Boolean>> rets = new ArrayList<>(logEvents.size());
        logEvents.forEach(logEvent -> {
            Callable<Boolean> task = () -> processLogEvent(logEvent);
            rets.add(task);
            threadPoolExecutor.submit(task);
        });
        boolean processRet = true;
        for (Callable<Boolean> ret : rets) {
            try {
                processRet &= ret.call();
            } catch (Exception e) {
                processRet = false;
                log.error("process log event exception: ", e);
            }
        }
        if (!processRet) {
            log.error("process log event for blockHeight: {} to: {} has some exception", lastProcessBlockHeight, blockHeightEnd);
        }
        lastProcessBlockHeight = blockHeightEnd;
        saveBlockHeight(chainConfig.getChainId(), lastProcessBlockHeight);
    }

    private void saveBlockHeight(Long chainId, Long blockHeight) {
        BlockHeightSignEntity entity = new BlockHeightSignEntity();
        entity.setBlockHeight(blockHeight);
        entity.setChainId(chainId);
        blockHeightSignDAO.save(entity);
    }

    private boolean processLogEvent(Log logEvent) {
        return onChainResultService.processLogEvent(logEvent);
    }

}
