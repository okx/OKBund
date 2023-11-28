package com.okcoin.dapp.bundler.rest.constant;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;

@Getter
public enum BundlingModeEnum {

    MANUAL("manual"),
    AUTO("auto");


    private static final Map<String, BundlingModeEnum> HOLDER = Maps.newHashMap();

    static {
        HOLDER.put(MANUAL.mode, MANUAL);
        HOLDER.put(AUTO.mode, AUTO);
    }

    private final String mode;

    BundlingModeEnum(String mode) {
        this.mode = mode;
    }

    public static BundlingModeEnum resolveBy(String mode) {
        return HOLDER.get(mode);
    }

}
