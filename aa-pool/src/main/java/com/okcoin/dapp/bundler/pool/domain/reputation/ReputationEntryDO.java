package com.okcoin.dapp.bundler.pool.domain.reputation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName ReputationDO
 * @Author qunqin
 * @Date 2023/10/24
 **/
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReputationEntryDO implements Serializable {

    private String address;

    private Integer opsSeen;

    private Integer opsIncluded;

    public static ReputationEntryDO newReputationEntryDO(String addr) {
        ReputationEntryDO entry = new ReputationEntryDO();
        entry.setAddress(addr);
        entry.setOpsSeen(0);
        entry.setOpsIncluded(0);
        return entry;
    }
}
