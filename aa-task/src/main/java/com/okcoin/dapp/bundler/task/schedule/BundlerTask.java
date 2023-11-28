package com.okcoin.dapp.bundler.task.schedule;

import com.okcoin.dapp.bundler.pool.bundler.IBundleService;
import com.okcoin.dapp.bundler.pool.config.PoolConfig;
import lombok.extern.slf4j.Slf4j;
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
    private IBundleService bundleService;
    @Resource
    private PoolConfig poolConfig;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void sendUopOnChain() {
        if (!timeToExe()) {
            return;
        }
        bundleService.sendNextBundle();

    }

    private boolean timeToExe() {
        int defaultInterval = 10;
        int autoExeTime = poolConfig.getAutoBundleInterval();
        if (autoExeTime <= 0) {
            autoExeTime = defaultInterval;
        }
        int currSecond = Calendar.getInstance().get(Calendar.SECOND);
        return currSecond % autoExeTime == 0;
    }

}
