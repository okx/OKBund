package com.okcoin.dapp.bundler.pool.domain.event;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.event.IEvent;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.util.List;

/**
 * this event is uop execute failed as revert, is normal failed event
 * log event topic[0] 0x1c4fada7374c0a9ee8841fc38afe82932dc0f8e69012e927f061a8bae611a201
 */
@Getter
public class UserOperationRevertReason implements IEvent {

    public static final Event EVENT = new Event("UserOperationRevertReason", Lists.newArrayList(
            TypeReference.create(Bytes32.class, true), TypeReference.create(Address.class, true),
            TypeReference.create(Uint256.class), TypeReference.create(DynamicBytes.class)));

    public static final String EVENT_SIG = EventEncoder.encode(EVENT);

    private final String userOpHash;

    private final String sender;

    private final int nonce;

    private final String revertReason;

    private final Log log;

    public UserOperationRevertReason(Log log) {
        this.log = log;
        EventValues eventValues = Contract.staticExtractEventParameters(EVENT, log);
        List<Type> indexedValues = eventValues.getIndexedValues();
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        userOpHash = Numeric.toHexString(((Bytes32) indexedValues.get(0)).getValue());
        sender = ((Address) indexedValues.get(1)).getValue();
        nonce = ((Uint256) nonIndexedValues.get(0)).getValue().intValue();
        revertReason = Numeric.toHexString(((DynamicBytes) nonIndexedValues.get(1)).getValue());
    }

    public static boolean isMatch(Log log) {
        return EVENT_SIG.equals(log.getTopics().get(0));
    }

}
