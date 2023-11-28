package com.okcoin.dapp.bundler.task.schedule;

import com.okcoin.dapp.bundler.pool.reputation.ReputationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author fanweiqiang
 * @create 2023/11/1 15:51
 */
@Slf4j
@Component
public class ReputationTask {

    @Resource
    private ReputationService reputationService;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void cronDecreaseSeenAndIncluded() {
        log.info("start task cronDecreaseSeenAndIncluded!");
        reputationService.decreaseSeenAndIncluded();
        log.info("end task cronDecreaseSeenAndIncluded!");
    }
}
