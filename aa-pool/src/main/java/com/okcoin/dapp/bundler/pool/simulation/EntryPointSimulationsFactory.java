package com.okcoin.dapp.bundler.pool.simulation;

import com.okcoin.dapp.bundler.pool.simulation.v6.EntryPointSimulationsV6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EntryPointSimulationsFactory {

    @Autowired
    private Map<String, IEntryPointSimulations> entryPointSimulationsMap;

    public IEntryPointSimulations get(String entryPoint) {
        return entryPointSimulationsMap.get(EntryPointSimulationsV6.VERSION);
    }

}
