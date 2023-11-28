package com.okcoin.dapp.bundler.infra.storage.dao;

import com.okcoin.dapp.bundler.infra.storage.AbstractTemplate;
import com.okcoin.dapp.bundler.infra.storage.DTO.BlockHeightSignEntity;
import org.springframework.stereotype.Component;

/**
 * @Author fanweiqiang
 * @create 2023/10/25 15:09
 */
@Component
public class BlockHeightSignDAO extends AbstractTemplate<BlockHeightSignEntity> {
    @Override
    public String getTableName() {
        return "/block_height_sign";
    }

    @Override
    public String getSaveKey(BlockHeightSignEntity blockHeightSignEntity) {
        return String.valueOf(blockHeightSignEntity.getChainId());
    }

    @Override
    public Class<BlockHeightSignEntity> getTClass() {
        return BlockHeightSignEntity.class;
    }
}
