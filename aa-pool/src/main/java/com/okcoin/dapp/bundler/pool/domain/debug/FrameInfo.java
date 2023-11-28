package com.okcoin.dapp.bundler.pool.domain.debug;

import com.okcoin.dapp.bundler.pool.constant.OpCodeConstant;
import lombok.Data;

import java.math.BigInteger;

@Data
public class FrameInfo {

    private String type;

    private String from;

    private String to;

    private String method;

    private BigInteger value;

    private Integer gas;

    private Integer gasUsed;

    private String data;

    public boolean isExit() {
        return OpCodeConstant.REVERT.equals(type) || OpCodeConstant.RETURN.equals(type);
    }
}
