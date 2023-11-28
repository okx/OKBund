package com.okcoin.dapp.bundler.task.schedule;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.storage.AbstractTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author fanweiqiang
 * @create 2023/10/26 14:04
 */
@Slf4j
@Component
public class SynMemoryToDbTask implements ApplicationContextAware {

    List<AbstractTemplate> templates = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractTemplate> beanMap = applicationContext.getBeansOfType(AbstractTemplate.class);
        templates = Lists.newArrayList(beanMap.values());
    }


    //    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void synToDb() {
        templates.forEach(dao -> {
            try {
                dao.synToDb();
            } catch (Exception e) {
                log.error("failed syn data to db for dao: {}, exception: {}", dao.getClass().getSimpleName(), e.getLocalizedMessage());
            }
        });
    }
}
