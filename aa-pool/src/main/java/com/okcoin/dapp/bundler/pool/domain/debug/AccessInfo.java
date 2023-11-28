package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.Map;

@Data
public class AccessInfo {

    private Map<String, String> reads;

    private Map<String, Integer> writes;
}
