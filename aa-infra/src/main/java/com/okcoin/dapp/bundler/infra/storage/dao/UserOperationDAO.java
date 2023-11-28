package com.okcoin.dapp.bundler.infra.storage.dao;

import com.okcoin.dapp.bundler.infra.storage.AbstractTemplate;
import com.okcoin.dapp.bundler.infra.storage.DTO.UserOperationEntity;
import org.springframework.stereotype.Component;

/**
 * @Author fanweiqiang
 * @create 2023/10/20 16:13
 */
@Component
public class UserOperationDAO extends AbstractTemplate<UserOperationEntity> {

    private String userOperateTable = "/test_uop";

    @Override
    public String getTableName() {
        return userOperateTable;
    }

    @Override
    public String getSaveKey(UserOperationEntity userOperationEntity) {
        return userOperationEntity.getUopHash();
    }

    @Override
    public Class<UserOperationEntity> getTClass() {
        return UserOperationEntity.class;
    }

}
