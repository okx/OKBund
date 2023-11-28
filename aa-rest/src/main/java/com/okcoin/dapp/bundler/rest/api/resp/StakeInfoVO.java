package com.okcoin.dapp.bundler.rest.api.resp;

import lombok.Data;

/**
 * @author Yukino.Xin on 2023/10/31 10:47
 */
@Data
public class StakeInfoVO {

    private String addr;

    private String stake;

    private String unstakeDelaySec;

}
