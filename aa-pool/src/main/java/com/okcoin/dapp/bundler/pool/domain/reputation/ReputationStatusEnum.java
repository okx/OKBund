package com.okcoin.dapp.bundler.pool.domain.reputation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ReputationEnum
 * @Author qunqin
 * @Date 2023/10/24
 **/
@Getter
@AllArgsConstructor
public enum ReputationStatusEnum {

    OK(0),

    THROTTLED(1),

    BANNED(2);

    private final int code;
}
