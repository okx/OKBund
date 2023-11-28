package com.okcoin.dapp.bundler.task.schedule;

import com.okcoin.dapp.bundler.task.logevent.LogEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author fanweiqiang
 * @create 2023/10/25 15:24
 */
@Slf4j
@Component
public class LogEventTask {
    @Resource
    private LogEventService logEventService;

    //    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void fetchContractLogs() {
        logEventService.handlePastEvents();
    }
}
