package com.okcoin.dapp.bundler.task.schedule;

import com.okcoin.dapp.bundler.pool.bundler.ExecutionService;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName BundlerTask
 * @Author qunqin
 * @Date 2023/10/25
 **/
@Slf4j
@Component
public class BundlerTask {

    @Resource
    private PoolConfig poolConfig;

    @Autowired
    private ExecutionService executionService;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void sendUopOnChain() {
        if (!timeToExe()) {
            return;
        }

        executionService.attemptBundle(true);
    }

    private boolean timeToExe() {
        int autoExeTime = poolConfig.getAutoBundleInterval();
        if (autoExeTime <= 0) {
            return false;
        }
        int currSecond = Calendar.getInstance().get(Calendar.MILLISECOND);
        return currSecond % autoExeTime == 0;
    }

}
