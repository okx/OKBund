package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.List;

@Data
public class LogInfo {

    private List<String> topics;

    private String data;
}
