package com.okcoin.dapp.bundler.pool.domain.pool;


import com.okcoin.dapp.bundler.pool.domain.UserOperationDO;
import com.okcoin.dapp.bundler.pool.domain.debug.ReferencedCodeHashes;
import lombok.Getter;

@Getter
public class MempoolEntry {

    private final UserOperationDO uop;

    private final ReferencedCodeHashes referencedContracts;

    public MempoolEntry(UserOperationDO uop, ReferencedCodeHashes referencedContracts) {
        this.uop = uop;
        this.referencedContracts = referencedContracts;
    }

    public MempoolEntryKey buildKey() {
        return new MempoolEntryKey(uop.getSender(), uop.getNonce(), uop.getMaxPriorityFeePerGas());
    }
}
