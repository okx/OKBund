package com.okcoin.dapp.bundler.pool.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AAExceptionEnum {

    SIMULATE_VALIDATION(-32500),

    SIMULATE_PAYMASTER_VALIDATION(-32501),

    OPCODE_VALIDATION(-32502),

    NOT_IN_TIME_RANGE(-32503),

    REPUTATION(-32504),

    INSUFFICIENT_STAKE(-32505),

    UNSUPPORTED_SIGNATURE_AGGREGATOR(-32506),

    INVALID_SIGNATURE(-32507),

    USER_OPERATION_REVERTED(-32521),

    METHOD_NOT_SUPPORT(-32601),

    INVALID_FIELDS(-32602),

    ;

    private final int code;

}
