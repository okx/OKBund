package com.okcoin.dapp.bundler.infra.storage.dao;

import com.okcoin.dapp.bundler.infra.storage.AbstractTemplate;
import com.okcoin.dapp.bundler.infra.storage.DTO.UopAndTxMapEntity;
import org.springframework.stereotype.Component;

@Component
public class UopAndTxMapDAO extends AbstractTemplate<UopAndTxMapEntity> {

    public String getTxHashBy(String uopHash) {
        UopAndTxMapEntity obj = getObj(uopHash);
        if (obj == null) {
            return null;
        }
        return obj.getTxHash();
    }

    @Override
    public String getTableName() {
        return "/uop_tx_map";
    }

    @Override
    public String getSaveKey(UopAndTxMapEntity uopAndTxMapEntity) {
        return uopAndTxMapEntity.getOpHash();
    }

    @Override
    public Class<UopAndTxMapEntity> getTClass() {
        return UopAndTxMapEntity.class;
    }
}
