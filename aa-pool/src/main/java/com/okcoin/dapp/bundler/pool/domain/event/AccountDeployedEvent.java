package com.okcoin.dapp.bundler.pool.domain.event;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.event.IEvent;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.util.List;

/**
 * @Author fanweiqiang
 * @create 2023/10/30 14:54
 */
@Getter
public class AccountDeployedEvent implements IEvent {

    public static final Event EVENT = new Event("UserOperationEvent", Lists.newArrayList(
            TypeReference.create(Bytes32.class, true), TypeReference.create(Address.class, true),
            TypeReference.create(Address.class), TypeReference.create(Address.class)));

    public static final String EVENT_SIG = EventEncoder.encode(EVENT);

    private final String userOpHash;

    private final String sender;

    private final String factory;

    private final String paymaster;

    private final Log log;

    public AccountDeployedEvent(Log logEvent) {
        this.log = logEvent;
        EventValues eventValues = Contract.staticExtractEventParameters(EVENT, log);
        List<Type> indexedValues = eventValues.getIndexedValues();
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        userOpHash = Numeric.toHexString(((Bytes32) (indexedValues.get(0))).getValue());
        sender = ((Address) indexedValues.get(1)).getValue();
        factory = ((Address) nonIndexedValues.get(0)).getValue();
        paymaster = ((Address) nonIndexedValues.get(1)).getValue();
    }

    public static boolean isMatched(Log logEvent) {
        return EVENT_SIG.equals(logEvent.getTopics().get(0));
    }

}
