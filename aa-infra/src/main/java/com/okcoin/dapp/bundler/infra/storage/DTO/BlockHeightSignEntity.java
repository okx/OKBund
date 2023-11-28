package com.okcoin.dapp.bundler.infra.storage.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author fanweiqiang
 * @create 2023/10/25 15:06
 */
@Data
public class BlockHeightSignEntity implements Serializable {
    private Long chainId;
    private Long blockHeight;
}
