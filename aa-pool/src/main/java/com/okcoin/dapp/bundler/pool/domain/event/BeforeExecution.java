package com.okcoin.dapp.bundler.pool.domain.event;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.event.IEvent;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.methods.response.Log;

/**
 * @author Yukino.Xin on 2023/10/30 13:51
 */

@Getter
public class BeforeExecution implements IEvent {

    public static final Event EVENT = new Event("BeforeExecution", Lists.newArrayList());

    public static final String EVENT_SIG = EventEncoder.encode(EVENT);

    private final Log log;

    public BeforeExecution(Log log) {
        this.log = log;
    }

    public static boolean isMatch(Log log) {
        return EVENT_SIG.equals(log.getTopics().get(0));
    }
}
