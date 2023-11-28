package com.okcoin.dapp.bundler.pool.domain.event;

import com.google.common.collect.Lists;
import com.okcoin.dapp.bundler.infra.chain.event.IEvent;
import lombok.Getter;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

/**
 * log event topic[0] 0x49628fd1471006c1482da88028e9ce4dbb080b815c9b0344d39e5a8e6ec1419f
 */
@Getter
public class UserOperationEvent implements IEvent {

    public static final Event EVENT = new Event("UserOperationEvent", Lists.newArrayList(
            TypeReference.create(Bytes32.class, true), TypeReference.create(Address.class, true),
            TypeReference.create(Address.class, true), TypeReference.create(Uint256.class),
            TypeReference.create(Bool.class), TypeReference.create(Uint256.class), TypeReference.create(Uint256.class)));

    public static final String EVENT_SIG = EventEncoder.encode(EVENT);

    private final String userOpHash;

    private final String sender;

    private final String paymaster;

    private final BigInteger nonce;

    private final boolean success;

    private final BigInteger actualGasCost;

    private final BigInteger actualGasUsed;

    private final Log log;

    public UserOperationEvent(Log log) {
        this.log = log;
        EventValues eventValues = Contract.staticExtractEventParameters(EVENT, log);
        List<Type> indexedValues = eventValues.getIndexedValues();
        List<Type> nonIndexedValues = eventValues.getNonIndexedValues();
        userOpHash = Numeric.toHexString(((Bytes32) (indexedValues.get(0))).getValue());
        sender = ((Address) indexedValues.get(1)).getValue();
        paymaster = ((Address) indexedValues.get(2)).getValue();
        nonce = ((Uint256) nonIndexedValues.get(0)).getValue();
        success = ((Bool) nonIndexedValues.get(1)).getValue();
        actualGasCost = ((Uint256) nonIndexedValues.get(2)).getValue();
        actualGasUsed = ((Uint256) nonIndexedValues.get(3)).getValue();
    }

    public static boolean isMatch(Log log) {
        return EVENT_SIG.equals(log.getTopics().get(0));
    }

}
