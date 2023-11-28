package com.okcoin.dapp.bundler.pool.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum UserOperationStatusEnum {

    REVERT(-3),

    TIMEOUT(-2),

    CANCELED(-1),

    WAITING(0),

    PENDING(1),

    BROADCASTING(2),

    SUCCEED(3),

    ;

    private final int code;

    public static UserOperationStatusEnum resolveBy(int status) {
        for (UserOperationStatusEnum statusEnum : values()) {
            if (statusEnum.getCode() == status) {
                return statusEnum;
            }
        }

        return null;
    }
}
