package com.okcoin.dapp.bundler.pool.exception;

import com.alibaba.fastjson2.JSON;
import com.okcoin.dapp.bundler.pool.util.MessageUtil;
import lombok.Getter;

@Getter
public class AAException extends RuntimeException {

    private final int code;

    private final String msg;

    private final Object data;

    public AAException(AAExceptionEnum exceptEnum, String format, Object... args) {
        super(exceptEnum.getCode() + "::" + MessageUtil.format(format, args));
        this.code = exceptEnum.getCode();
        this.msg = MessageUtil.format(format, args);
        this.data = null;
    }

    public AAException(Object data, AAExceptionEnum exceptEnum, String format, Object... args) {
        super(exceptEnum.getCode() + "::" + MessageUtil.format(format, args)
                + (data == null ? "" : "::"
                + (data instanceof String ? data : JSON.toJSONString(data))));
        this.code = exceptEnum.getCode();
        this.msg = MessageUtil.format(format, args);

        if (data instanceof String && JSON.isValidObject((String) data)) {
            this.data = JSON.parseObject((String) data);
        } else {
            this.data = data;
        }
    }
}
