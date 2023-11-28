package com.okcoin.dapp.bundler.rest.api.req;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Yukino.Xin on 2023/10/30 15:56
 */

@Data
public class ReputationEntryParam {

    private String address;

    private Integer opsSeen;

    private Integer opsIncluded;

    private String status;

    public void setAddress(String address) {
        this.address = StringUtils.lowerCase(address);
    }
}
