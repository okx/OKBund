package com.okcoin.dapp.bundler.rest.gas;


import com.okcoin.dapp.bundler.infra.chain.IChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GasEstimatorFactory {

    @Autowired
    private List<IGasEstimator> gasEstimators;

    public IGasEstimator get(IChain chain) {
        for (IGasEstimator gasEstimator : gasEstimators) {
            if (!gasEstimator.fit(chain)) {
                continue;
            }

            return gasEstimator;
        }

        return null;

    }

}
