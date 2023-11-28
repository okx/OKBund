package com.okcoin.dapp.bundler.pool.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.okcoin.dapp.bundler.pool.domain.event.BeforeExecution;
import com.okcoin.dapp.bundler.pool.domain.event.UserOperationEvent;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Log;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yukino.Xin on 2023/10/30 13:55
 */

@Service
public class EventService {

    public LinkedHashMap<String, List<Log>> groupUserOperationLog(String entryPoint, List<Log> logs) {
        LinkedHashMap<String, List<Log>> uopEventMap = Maps.newLinkedHashMap();
        int opIndex = -1;
        int preIndex = -1;
        for (int i = 0; i < logs.size(); i++) {
            Log logEvent = logs.get(i);
            if (BeforeExecution.isMatch(logEvent)) {
                opIndex = i;
                preIndex = i;
                continue;
            }

            if (opIndex == -1) {
                continue;
            }

            if (logEvent.getAddress().equals(entryPoint) && (UserOperationEvent.isMatch(logEvent))) {
                UserOperationEvent event = new UserOperationEvent(logEvent);
                uopEventMap.put(event.getUserOpHash(), Lists.newArrayList(logs.subList(preIndex, preIndex = i + 1)));
            }
        }
        return uopEventMap;
    }
}
