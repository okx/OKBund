package com.okcoin.dapp.bundler.pool.domain.debug;

import lombok.Data;

import java.util.List;


@Data
public class BundlerCollectorReturn {

    private List<TopLevelCallInfo> callsFromEntryPoint;

    private List<String> keccak;

    private List<FrameInfo> calls;

    private List<LogInfo> logs;

    private List<Object> debug;

}
