package com.okcoin.dapp.bundler.infra.chain.web3j.resp;

import lombok.Data;

import java.util.List;

@Data
public class CallTraceTransaction {

    /**
     * The type of the call
     */
    private String type;
    /**
     * The address the transaction is sent from
     */
    private String from;
    /**
     * The address the transaction is directed to
     */
    private String to;
    /**
     * The integer of the value sent with this transaction
     */
    private String value;

    /**
     * The integer of the gas provided for the transaction execution
     */
    private String gas;
    /**
     * The integer of the gas used
     */
    private String gasUsed;
    /**
     * The data given at the time of input
     */
    private String input;
    /**
     * The data which is returned as an output
     */
    private String output;
    /**
     * The type of error, if any
     */
    private String error;
    /**
     * The type solidity revert reason, if any
     */
    private String revertReason;
    /**
     * A list of sub-calls
     */
    private List<CallTraceTransaction> calls;
}
