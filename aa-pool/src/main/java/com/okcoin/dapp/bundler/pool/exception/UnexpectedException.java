package com.okcoin.dapp.bundler.pool.exception;

import com.okcoin.dapp.bundler.pool.util.MessageUtil;
import lombok.Getter;

@Getter
public class UnexpectedException extends RuntimeException {

    private final String msg;

    public UnexpectedException(String format, Object... args) {
        super(MessageUtil.format(format, args));
        this.msg = MessageUtil.format(format, args);
    }
}
