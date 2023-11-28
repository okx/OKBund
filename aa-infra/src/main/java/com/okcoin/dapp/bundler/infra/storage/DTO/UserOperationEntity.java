package com.okcoin.dapp.bundler.infra.storage.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author fanweiqiang
 * @create 2023/10/20 15:24
 */
@Data
public class UserOperationEntity implements Serializable {

    private String uopHash;
}
