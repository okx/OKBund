package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CallEntry {
    private String to;
    private String from;
    private String type; // call opcode
    private String method; // parsed method, or signash if unparsed
    private String revert; // parsed output from REVERT
    private String result; // parsed method output.
    private BigInteger value;

    public CallEntry(String to, String from, String type, String method, BigInteger value) {
        this.to = to;
        this.from = from;
        this.type = type;
        this.method = method;
        this.value = value;
    }
}
